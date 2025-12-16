# Frontend Integration Guide

This guide shows how to integrate the Payment Service with your frontend application.

## Overview

The payment flow involves two main API calls:

1. **Create Checkout** - Get Stripe checkout URL
2. **Verify Payment** - Confirm payment status after redirect

## Request Data Structure

```javascript
{
  "orderNumber": "ORD-12345",        // Required: Unique order identifier
  "customerEmail": "user@example.com", // Optional: Customer email for Stripe
  "customerId": 1,                    // Required: Customer ID
  "totalAmount": 99.99,               // Required: Total payment amount
  "shippingAddress": "123 Main St"   // Optional: Shipping address
}
```

## Step-by-Step Integration

### Step 1: Create Checkout Session

When the user clicks "Checkout" or "Pay Now", call the checkout endpoint:

```javascript
// Example using fetch API
async function createCheckout(orderData) {
  try {
    const response = await fetch(
      "http://localhost:8080/api/v1/payments/checkout",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          orderNumber: orderData.orderNumber,
          customerEmail: orderData.customerEmail,
          customerId: orderData.customerId,
          totalAmount: orderData.totalAmount,
          shippingAddress: orderData.shippingAddress,
        }),
      }
    );

    if (!response.ok) {
      throw new Error("Failed to create checkout session");
    }

    const data = await response.json();

    // Redirect user to Stripe checkout page
    window.location.href = data.stripeCheckoutUrl;
  } catch (error) {
    console.error("Checkout error:", error);
    // Show error message to user
  }
}
```

### Step 2: Handle Success Redirect

Create a success page that handles the redirect from Stripe:

```javascript
// success.js or success page component
async function handlePaymentSuccess() {
  // Get session_id from URL query parameters
  const urlParams = new URLSearchParams(window.location.search);
  const sessionId = urlParams.get("session_id");

  if (!sessionId) {
    console.error("No session ID found");
    return;
  }

  try {
    // Verify payment with backend
    const response = await fetch(
      `http://localhost:8080/api/v1/payments/verify?session_id=${sessionId}`
    );

    if (!response.ok) {
      throw new Error("Failed to verify payment");
    }

    const paymentData = await response.json();

    // Check payment status
    if (paymentData.status === "COMPLETED") {
      // Payment successful!
      showSuccessMessage(paymentData);
      // Update order status, show confirmation, etc.
    } else {
      // Payment not completed
      showPendingMessage(paymentData);
    }
  } catch (error) {
    console.error("Verification error:", error);
    showErrorMessage();
  }
}

// Call this when the success page loads
window.addEventListener("load", handlePaymentSuccess);
```

### Step 3: Handle Cancel Redirect

Create a cancel page for when users cancel the payment:

```javascript
// cancel.js or cancel page component
function handlePaymentCancel() {
  // Show cancellation message
  showMessage("Payment was cancelled. You can try again when ready.");

  // Optionally redirect back to cart or checkout page
  setTimeout(() => {
    window.location.href = "/cart";
  }, 3000);
}

window.addEventListener("load", handlePaymentCancel);
```

## React Example

### Checkout Component

```jsx
import { useState } from "react";

function CheckoutButton({ orderData }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleCheckout = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(
        "http://localhost:8080/api/v1/payments/checkout",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            orderNumber: orderData.orderNumber,
            customerEmail: orderData.customerEmail,
            customerId: orderData.customerId,
            totalAmount: orderData.totalAmount,
            shippingAddress: orderData.shippingAddress,
          }),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to create checkout session");
      }

      const data = await response.json();

      // Redirect to Stripe
      window.location.href = data.stripeCheckoutUrl;
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  return (
    <div>
      <button
        onClick={handleCheckout}
        disabled={loading}
        className="checkout-button"
      >
        {loading ? "Processing..." : "Proceed to Payment"}
      </button>
      {error && <p className="error">{error}</p>}
    </div>
  );
}

export default CheckoutButton;
```

### Success Page Component

```jsx
import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";

function PaymentSuccess() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState("verifying");
  const [paymentData, setPaymentData] = useState(null);

  useEffect(() => {
    const sessionId = searchParams.get("session_id");

    if (!sessionId) {
      setStatus("error");
      return;
    }

    verifyPayment(sessionId);
  }, [searchParams]);

  const verifyPayment = async (sessionId) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/payments/verify?session_id=${sessionId}`
      );

      if (!response.ok) {
        throw new Error("Verification failed");
      }

      const data = await response.json();
      setPaymentData(data);

      if (data.status === "COMPLETED") {
        setStatus("success");
        // Redirect to orders page after 3 seconds
        setTimeout(() => navigate("/orders"), 3000);
      } else {
        setStatus("pending");
      }
    } catch (error) {
      console.error("Verification error:", error);
      setStatus("error");
    }
  };

  return (
    <div className="payment-success-container">
      {status === "verifying" && (
        <div>
          <h2>Verifying your payment...</h2>
          <p>Please wait while we confirm your payment.</p>
        </div>
      )}

      {status === "success" && paymentData && (
        <div>
          <h2>✅ Payment Successful!</h2>
          <p>Thank you for your payment.</p>
          <div className="payment-details">
            <p>
              <strong>Order Number:</strong> {paymentData.orderNumber}
            </p>
            <p>
              <strong>Amount:</strong> ${paymentData.totalAmount}
            </p>
            <p>
              <strong>Status:</strong> {paymentData.status}
            </p>
          </div>
          <p>Redirecting to your orders...</p>
        </div>
      )}

      {status === "pending" && (
        <div>
          <h2>⏳ Payment Pending</h2>
          <p>Your payment is being processed. Please check back later.</p>
        </div>
      )}

      {status === "error" && (
        <div>
          <h2>❌ Verification Error</h2>
          <p>We couldn't verify your payment. Please contact support.</p>
          <button onClick={() => navigate("/orders")}>Go to Orders</button>
        </div>
      )}
    </div>
  );
}

export default PaymentSuccess;
```

### Cancel Page Component

```jsx
import { useNavigate } from "react-router-dom";

function PaymentCancel() {
  const navigate = useNavigate();

  return (
    <div className="payment-cancel-container">
      <h2>❌ Payment Cancelled</h2>
      <p>You cancelled the payment process.</p>
      <p>No charges were made to your account.</p>

      <div className="actions">
        <button onClick={() => navigate("/cart")}>Return to Cart</button>
        <button onClick={() => navigate("/checkout")}>Try Again</button>
      </div>
    </div>
  );
}

export default PaymentCancel;
```

## Next.js Example

### API Route (Optional - for server-side calls)

```javascript
// pages/api/checkout.js
export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ message: "Method not allowed" });
  }

  try {
    const response = await fetch(
      "http://localhost:8080/api/v1/payments/checkout",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(req.body),
      }
    );

    const data = await response.json();
    res.status(response.status).json(data);
  } catch (error) {
    res.status(500).json({ message: "Internal server error" });
  }
}
```

## Environment Variables

Add these to your frontend `.env` file:

```bash
# .env.local (Next.js) or .env (React)
NEXT_PUBLIC_PAYMENT_API_URL=http://localhost:8080
# or
REACT_APP_PAYMENT_API_URL=http://localhost:8080
```

Then use in your code:

```javascript
const API_URL =
  process.env.NEXT_PUBLIC_PAYMENT_API_URL ||
  process.env.REACT_APP_PAYMENT_API_URL;

const response = await fetch(`${API_URL}/api/v1/payments/checkout`, {
  // ...
});
```

## API Response Examples

### Checkout Response

```json
{
  "id": 1,
  "orderNumber": "ORD-12345",
  "customerId": 1,
  "customerEmail": "user@example.com",
  "totalAmount": 99.99,
  "shippingAddress": "123 Main Street, New York, NY 10001",
  "stripeCheckoutUrl": "https://checkout.stripe.com/c/pay/cs_test_...",
  "status": "PENDING",
  "createdAt": "2025-12-15T22:00:00",
  "updatedAt": "2025-12-15T22:00:00"
}
```

### Verify Payment Response

```json
{
  "id": 1,
  "orderNumber": "ORD-12345",
  "customerId": 1,
  "customerEmail": "user@example.com",
  "totalAmount": 99.99,
  "shippingAddress": "123 Main Street, New York, NY 10001",
  "status": "COMPLETED",
  "createdAt": "2025-12-15T22:00:00",
  "updatedAt": "2025-12-15T22:05:00"
}
```

## CORS Configuration

If you encounter CORS issues, add this to your Spring Boot application:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000") // Your frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

## Error Handling Best Practices

```javascript
async function handleCheckout(orderData) {
  try {
    const response = await fetch(
      "http://localhost:8080/api/v1/payments/checkout",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(orderData),
      }
    );

    // Parse response
    const data = await response.json();

    // Check for errors
    if (!response.ok) {
      // Handle API errors
      if (data.validationErrors) {
        // Show field-specific errors
        Object.entries(data.validationErrors).forEach(([field, message]) => {
          showFieldError(field, message);
        });
      } else {
        // Show general error
        showError(data.message || "Checkout failed");
      }
      return;
    }

    // Success - redirect to Stripe
    window.location.href = data.stripeCheckoutUrl;
  } catch (error) {
    // Network or other errors
    console.error("Checkout error:", error);
    showError("Unable to connect to payment service. Please try again.");
  }
}
```

## Testing

### Test Data

Use these values for testing:

```javascript
const testOrder = {
  orderNumber: `ORD-${Date.now()}`, // Unique order number
  customerEmail: "test@example.com",
  customerId: 1,
  totalAmount: 99.99,
  shippingAddress: "123 Test Street, Test City, TC 12345",
};
```

### Stripe Test Cards

When redirected to Stripe checkout, use these test cards:

- **Success**: `4242 4242 4242 4242`
- **Decline**: `4000 0000 0000 0002`
- **Requires Auth**: `4000 0025 0000 3155`

Use any future expiry date, any 3-digit CVC, and any postal code.

## Payment Statuses

- `PENDING` - Checkout session created, payment not yet attempted
- `PROCESSING` - Payment is being processed
- `COMPLETED` - Payment successfully completed
- `FAILED` - Payment failed
- `CANCELLED` - Payment was cancelled by user

## Production Checklist

Before going to production:

- [ ] Update `STRIPE_SECRET_KEY` to production key (starts with `sk_live_`)
- [ ] Update success/cancel URLs to production URLs
- [ ] Enable HTTPS for all endpoints
- [ ] Implement proper authentication/authorization
- [ ] Add rate limiting
- [ ] Set up monitoring and alerts
- [ ] Test with real payment amounts
- [ ] Implement webhook handlers for payment events
- [ ] Add logging for audit trail
- [ ] Configure proper error tracking (e.g., Sentry)

## Common Issues

### Issue: CORS Error

**Solution**: Configure CORS in backend (see CORS Configuration section above)

### Issue: Redirect not working

**Solution**: Ensure `stripeCheckoutUrl` is being used correctly and is a valid URL

### Issue: Payment verification fails

**Solution**: Check that the `session_id` is being passed correctly in the URL

### Issue: Validation errors

**Solution**: Ensure all required fields are provided:

- `orderNumber` (required, not blank)
- `customerId` (required, not null)
- `totalAmount` (required, positive number)
- `customerEmail` (optional, but must be valid email format if provided)

## Environment Variables

Create a `.env` file in your payment service root:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payment_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT (if needed for other services)
JWT_SECRET=your-secret-key-here

# Stripe
STRIPE_SECRET_KEY=sk_test_your_stripe_key
STRIPE_SUCCESS_URL=http://localhost:3000/payment/success
STRIPE_CANCEL_URL=http://localhost:3000/payment/cancel
```

## Getting Your Stripe API Key

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/)
2. Navigate to **Developers** → **API keys**
3. Copy your **Secret key** (starts with `sk_test_` for test mode)
4. Set it as `STRIPE_SECRET_KEY` in your `.env` file

## Support

For issues or questions:

1. Check the Postman collection for API testing
2. Review Stripe Dashboard for payment details
3. Check application logs for errors
