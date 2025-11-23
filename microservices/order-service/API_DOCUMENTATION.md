# Order Service API Documentation


Base domain: `http://localhost:8080`
Base URL: `/api/orders`

## Endpoints

### 1. Get All Orders
- **URL**: `/api/orders`
- **Method**: `GET`
- **Description**: Retrieves a list of all orders.
- **Response Body**: `List<OrderDTO>`
  ```json
  [
    {
      "id": 1,
      "orderNumber": "ORD-12345",
      "customerId": 101,
      "totalAmount": 99.99,
      "status": "CREATED",
      "items": [
        {
          "productId": 501,
          "productName": "Widget",
          "quantity": 2
        }
      ],
      "shippingAddress": "123 Main St"
    }
  ]
  ```

### 2. Get Order by ID
- **URL**: `/api/orders/{id}`
- **Method**: `GET`
- **Description**: Retrieves a specific order by its unique ID.
- **Path Parameters**:
    - `id`: Long (Required)
- **Response Body**: `OrderDTO` (see above)

### 3. Get Order by Order Number
- **URL**: `/api/orders/number/{orderNumber}`
- **Method**: `GET`
- **Description**: Retrieves a specific order by its order number string.
- **Path Parameters**:
    - `orderNumber`: String (Required)
- **Response Body**: `OrderDTO` (see above)

### 4. Get Orders by Customer ID
- **URL**: `/api/orders/customer/{customerId}`
- **Method**: `GET`
- **Description**: Retrieves all orders for a specific customer.
- **Path Parameters**:
    - `customerId`: Long (Required)
- **Response Body**: `List<OrderDTO>`

### 5. Create Order
- **URL**: `/api/orders`
- **Method**: `POST`
- **Description**: Creates a new order.
- **Request Body**: `CreateOrderRequest`
  ```json
  {
    "customerId": 101,
    "items": [
      {
        "productId": 501,
        "productName": "Widget",
        "quantity": 2
      }
    ],
    "shippingAddress": "123 Main St"
  }
  ```
- **Response Body**: `OrderDTO` (Created order)

### 6. Update Order Status
- **URL**: `/api/orders/{id}/status`
- **Method**: `PATCH`
- **Description**: Updates the status of an existing order.
- **Path Parameters**:
    - `id`: Long (Required)
- **Request Body**: Map
  ```json
  {
    "status": "SHIPPED"
  }
  ```
- **Response Body**: `OrderDTO` (Updated order)

### 7. Cancel Order
- **URL**: `/api/orders/{id}`
- **Method**: `DELETE`
- **Description**: Cancels (deletes) an order.
- **Path Parameters**:
    - `id`: Long (Required)
- **Response Body**: None (204 No Content)
