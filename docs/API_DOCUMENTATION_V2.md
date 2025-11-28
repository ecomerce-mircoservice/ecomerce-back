# API Documentation V2

Complete API reference for the microservices e-commerce platform.

## Base URL

```
Gateway: http://localhost:8080
```

All requests go through the API Gateway which routes to the appropriate microservice.

---

## 1. Authentication Service (Auth Service)

Base Path: `/api/v1/auth`

### 1.1 Register User

**Endpoint:** `POST /api/v1/auth/register`

**Description:** Register a new user account

**Request Body:**

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "roles": "ROLE_USER"
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "roles": "ROLE_USER"
    }
  }
}
```

### 1.2 Login

**Endpoint:** `POST /api/v1/auth/login`

**Description:** Authenticate user and get JWT token

**Request Body:**

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "roles": "ROLE_USER"
    }
  }
}
```

### 1.3 Get Current User

**Endpoint:** `GET /api/v1/auth/me`

**Description:** Get authenticated user's information

**Headers:**

```
Authorization: Bearer <token>
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "User information retrieved successfully",
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "roles": "ROLE_USER"
  }
}
```

### 1.4 Refresh Token

**Endpoint:** `POST /api/v1/auth/refresh-token`

**Description:** Refresh JWT token

**Headers:**

```
Authorization: Bearer <token>
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Token refreshed successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

## 2. User Management Service (Admin Only)

Base Path: `/api/v1/users`

**Note:** All endpoints require `ROLE_ADMIN` authority

### 2.1 Get All Users (Paginated)

**Endpoint:** `GET /api/v1/users`

**Description:** Get paginated list of all users with optional search

**Query Parameters:**

- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Items per page
- `search` (optional) - Search by name or email

**Headers:**

```
Authorization: Bearer <admin-token>
```

**Example Request:**

```
GET /api/v1/users?page=0&size=10&search=john
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "roles": "ROLE_USER"
    }
  ],
  "metadata": {
    "pagination": {
      "page": 0,
      "pageSize": 10,
      "totalPages": 5,
      "totalItems": 50
    }
  }
}
```

### 2.2 Get User by ID

**Endpoint:** `GET /api/v1/users/{id}`

**Description:** Get specific user by ID

**Headers:**

```
Authorization: Bearer <admin-token>
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "roles": "ROLE_USER"
  }
}
```

### 2.3 Create User

**Endpoint:** `POST /api/v1/users`

**Description:** Create a new user (admin only)

**Headers:**

```
Authorization: Bearer <admin-token>
```

**Request Body:**

```json
{
  "name": "Jane Smith",
  "email": "jane@example.com",
  "password": "password123",
  "roles": "ROLE_USER"
}
```

**Response:** `201 CREATED`

```json
{
  "success": true,
  "code": 200,
  "message": "User created successfully",
  "data": {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane@example.com",
    "roles": "ROLE_USER"
  }
}
```

### 2.4 Update User

**Endpoint:** `PUT /api/v1/users/{id}`

**Description:** Update user information

**Headers:**

```
Authorization: Bearer <admin-token>
```

**Request Body:**

```json
{
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "roles": "ROLE_ADMIN"
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "User updated successfully",
  "data": {
    "id": 2,
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "roles": "ROLE_ADMIN"
  }
}
```

### 2.5 Delete User

**Endpoint:** `DELETE /api/v1/users/{id}`

**Description:** Delete a user

**Headers:**

```
Authorization: Bearer <admin-token>
```

**Response:** `204 NO CONTENT`

---

## 3. Product Service

Base Path: `/api/v1/products`

### 3.1 Get All Products

**Endpoint:** `GET /api/v1/products`

**Description:** Get products with optional pagination and search

**Query Parameters:**

- `page` (optional, default: 1) - Page number
- `limit` (optional, default: 10) - Items per page
- `search` (optional) - Search by product name
- `paginated` (optional, default: true) - Enable/disable pagination

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request (Paginated):**

```
GET /api/v1/products?page=1&limit=10&search=laptop
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Products retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Dell Laptop",
      "description": "High performance laptop",
      "price": 999.99,
      "stockQuantity": 50,
      "category": "Electronics",
      "imageUrl": "uploads/123456_laptop.jpg",
      "active": true,
      "rating": 4.5
    }
  ],
  "metadata": {
    "pagination": {
      "page": 1,
      "pageSize": 10,
      "totalPages": 3,
      "totalItems": 25
    }
  }
}
```

### 3.2 Get Product by ID

**Endpoint:** `GET /api/v1/products/{id}`

**Description:** Get specific product details

**Headers:**

```
Authorization: Bearer <token>
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Product retrieved successfully",
  "data": {
    "id": 1,
    "name": "Dell Laptop",
    "description": "High performance laptop",
    "price": 999.99,
    "stockQuantity": 50,
    "category": "Electronics",
    "imageUrl": "uploads/123456_laptop.jpg",
    "active": true,
    "rating": 4.5
  }
}
```

### 3.3 Search Products

**Endpoint:** `GET /api/v1/products/search`

**Description:** Search products by name

**Query Parameters:**

- `name` (required) - Product name to search

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request:**

```
GET /api/v1/products/search?name=laptop
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Search results retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Dell Laptop",
      "description": "High performance laptop",
      "price": 999.99,
      "stockQuantity": 50,
      "category": "Electronics",
      "imageUrl": "uploads/123456_laptop.jpg",
      "active": true,
      "rating": 4.5
    }
  ]
}
```

### 3.4 Get Products by Category

**Endpoint:** `GET /api/v1/products/category/{category}`

**Description:** Get all products in a specific category

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request:**

```
GET /api/v1/products/category/Electronics
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Products retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Dell Laptop",
      "description": "High performance laptop",
      "price": 999.99,
      "stockQuantity": 50,
      "category": "Electronics",
      "imageUrl": "uploads/123456_laptop.jpg",
      "active": true,
      "rating": 4.5
    }
  ]
}
```

### 3.5 Create Product

**Endpoint:** `POST /api/v1/products`

**Description:** Create a new product with image upload

**Content-Type:** `multipart/form-data`

**Headers:**

```
Authorization: Bearer <token>
```

**Form Data:**

- `name` (string, required) - Product name
- `description` (string, required) - Product description
- `price` (decimal, required) - Product price
- `stockQuantity` (integer, required) - Available stock
- `category` (string, required) - Product category
- `active` (boolean, optional, default: true) - Product active status
- `rating` (decimal, optional) - Product rating
- `image` (file, required) - Product image file

**Response:** `201 CREATED`

```json
{
  "success": true,
  "code": 200,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "Dell Laptop",
    "description": "High performance laptop",
    "price": 999.99,
    "stockQuantity": 50,
    "category": "Electronics",
    "imageUrl": "uploads/123456_laptop.jpg",
    "active": true,
    "rating": 4.5
  }
}
```

### 3.6 Update Product

**Endpoint:** `PUT /api/v1/products/{id}`

**Description:** Update existing product (optionally with new image)

**Content-Type:** `multipart/form-data`

**Headers:**

```
Authorization: Bearer <token>
```

**Form Data:**

- `name` (string, required) - Product name
- `description` (string, required) - Product description
- `price` (decimal, required) - Product price
- `stockQuantity` (integer, required) - Available stock
- `category` (string, required) - Product category
- `active` (boolean, optional, default: true) - Product active status
- `rating` (decimal, optional) - Product rating
- `image` (file, optional) - New product image (old one will be deleted)

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Product updated successfully",
  "data": {
    "id": 1,
    "name": "Dell Laptop Pro",
    "description": "High performance laptop with updated specs",
    "price": 1099.99,
    "stockQuantity": 45,
    "category": "Electronics",
    "imageUrl": "uploads/789012_laptop_new.jpg",
    "active": true,
    "rating": 4.7
  }
}
```

### 3.7 Delete Product

**Endpoint:** `DELETE /api/v1/products/{id}`

**Description:** Soft delete a product (sets active to false)

**Headers:**

```
Authorization: Bearer <token>
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Product deleted successfully",
  "data": null
}
```

### 3.8 Reserve Stock

**Endpoint:** `POST /api/v1/products/{id}/reserve`

**Description:** Reserve product stock (used during order creation)

**Query Parameters:**

- `quantity` (required) - Quantity to reserve

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request:**

```
POST /api/v1/products/1/reserve?quantity=2
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Stock reserved successfully",
  "data": {
    "reserved": true,
    "productId": 1,
    "quantity": 2
  }
}
```

### 3.9 Release Stock

**Endpoint:** `POST /api/v1/products/{id}/release`

**Description:** Release reserved stock (used when order is cancelled)

**Query Parameters:**

- `quantity` (required) - Quantity to release

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request:**

```
POST /api/v1/products/1/release?quantity=2
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Stock released successfully",
  "data": null
}
```

---

## 4. Cart Service

Base Path: `/api/v1/cart`

### 4.1 Get Current Cart

**Endpoint:** `GET /api/v1/cart/current`

**Description:** Get user's current shopping cart

**Query Parameters:**

- `userId` (required) - User ID

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request:**

```
GET /api/v1/cart/current?userId=1
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Cart retrieved successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "items": [
      {
        "product": {
          "id": 1,
          "name": "Dell Laptop",
          "description": "High performance laptop",
          "price": 999.99,
          "stockQuantity": 50,
          "category": "Electronics",
          "imageUrl": "uploads/123456_laptop.jpg",
          "active": true,
          "rating": 4.5
        },
        "quantity": 2,
        "subtotal": 1999.98
      }
    ],
    "total": 1999.98,
    "itemCount": 2
  }
}
```

### 4.2 Add Item to Cart

**Endpoint:** `POST /api/v1/cart/add`

**Description:** Add product to cart

**Headers:**

```
Authorization: Bearer <token>
```

**Request Body:**

```json
{
  "userId": 1,
  "productId": 1,
  "quantity": 2
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Item added to cart successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "items": [
      {
        "product": {
          "id": 1,
          "name": "Dell Laptop",
          "description": "High performance laptop",
          "price": 999.99,
          "stockQuantity": 50,
          "category": "Electronics",
          "imageUrl": "uploads/123456_laptop.jpg",
          "active": true,
          "rating": 4.5
        },
        "quantity": 2,
        "subtotal": 1999.98
      }
    ],
    "total": 1999.98,
    "itemCount": 2
  }
}
```

### 4.3 Update Cart Item

**Endpoint:** `POST /api/v1/cart/update`

**Description:** Update item quantity in cart

**Headers:**

```
Authorization: Bearer <token>
```

**Request Body:**

```json
{
  "userId": 1,
  "productId": 1,
  "quantity": 3
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Cart item updated successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "items": [
      {
        "product": {
          "id": 1,
          "name": "Dell Laptop",
          "description": "High performance laptop",
          "price": 999.99,
          "stockQuantity": 50,
          "category": "Electronics",
          "imageUrl": "uploads/123456_laptop.jpg",
          "active": true,
          "rating": 4.5
        },
        "quantity": 3,
        "subtotal": 2999.97
      }
    ],
    "total": 2999.97,
    "itemCount": 3
  }
}
```

### 4.4 Remove Item from Cart

**Endpoint:** `DELETE /api/v1/cart/items/{productId}`

**Description:** Remove specific item from cart

**Query Parameters:**

- `userId` (required) - User ID

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request:**

```
DELETE /api/v1/cart/items/1?userId=1
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Item removed from cart successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "items": [],
    "total": 0,
    "itemCount": 0
  }
}
```

### 4.5 Clear Cart

**Endpoint:** `DELETE /api/v1/cart/clear`

**Description:** Remove all items from cart

**Query Parameters:**

- `userId` (required) - User ID

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request:**

```
DELETE /api/v1/cart/clear?userId=1
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Cart cleared successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "items": [],
    "total": 0,
    "itemCount": 0
  }
}
```

---

## 5. Order Service

Base Path: `/api/v1/orders`

### 5.1 Get All Orders

**Endpoint:** `GET /api/v1/orders`

**Description:** Get all orders (admin only)

**Headers:**

```
Authorization: Bearer <token>
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Orders retrieved successfully",
  "data": [
    {
      "id": 1,
      "orderNumber": "ORD-1638345600000",
      "customerId": 1,
      "totalAmount": 1999.98,
      "status": "PENDING",
      "shippingAddress": {
        "fullName": "John Doe",
        "street": "123 Main St",
        "city": "New York",
        "state": "NY",
        "zipCode": "10001",
        "country": "USA"
      },
      "orderItems": [
        {
          "productId": 1,
          "quantity": 2,
          "price": 999.99,
          "subtotal": 1999.98
        }
      ],
      "createdAt": "2024-12-01T10:00:00",
      "updatedAt": "2024-12-01T10:00:00"
    }
  ]
}
```

### 5.2 Get Order by ID

**Endpoint:** `GET /api/v1/orders/{id}`

**Description:** Get specific order details

**Headers:**

```
Authorization: Bearer <token>
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Order retrieved successfully",
  "data": {
    "id": 1,
    "orderNumber": "ORD-1638345600000",
    "customerId": 1,
    "totalAmount": 1999.98,
    "status": "PENDING",
    "shippingAddress": {
      "fullName": "John Doe",
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    },
    "orderItems": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 999.99,
        "subtotal": 1999.98
      }
    ],
    "createdAt": "2024-12-01T10:00:00",
    "updatedAt": "2024-12-01T10:00:00"
  }
}
```

### 5.3 Get Order by Order Number

**Endpoint:** `GET /api/v1/orders/number/{orderNumber}`

**Description:** Get order by order number

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request:**

```
GET /api/v1/orders/number/ORD-1638345600000
```

**Response:** `200 OK` (same ApiResponse structure as Get Order by ID)

### 5.4 Get Customer Orders

**Endpoint:** `GET /api/v1/orders/customer/{customerId}`

**Description:** Get all orders for a specific customer

**Headers:**

```
Authorization: Bearer <token>
```

**Example Request:**

```
GET /api/v1/orders/customer/1
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Customer orders retrieved successfully",
  "data": [
    {
      "id": 1,
      "orderNumber": "ORD-1638345600000",
      "customerId": 1,
      "totalAmount": 1999.98,
      "status": "PENDING",
      "shippingAddress": {
        "fullName": "John Doe",
        "street": "123 Main St",
        "city": "New York",
        "state": "NY",
        "zipCode": "10001",
        "country": "USA"
      },
      "orderItems": [
        {
          "productId": 1,
          "quantity": 2,
          "price": 999.99,
          "subtotal": 1999.98
        }
      ],
      "createdAt": "2024-12-01T10:00:00",
      "updatedAt": "2024-12-01T10:00:00"
    }
  ]
}
```

### 5.5 Create Order

**Endpoint:** `POST /api/v1/orders`

**Description:** Create a new order from cart

**Headers:**

```
Authorization: Bearer <token>
```

**Request Body:**

```json
{
  "customerId": 1,
  "shippingAddress": {
    "fullName": "John Doe",
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 999.99
    }
  ]
}
```

**Response:** `201 CREATED`

```json
{
  "success": true,
  "code": 200,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "orderNumber": "ORD-1638345600000",
    "customerId": 1,
    "totalAmount": 1999.98,
    "status": "PENDING",
    "shippingAddress": {
      "fullName": "John Doe",
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    },
    "orderItems": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 999.99,
        "subtotal": 1999.98
      }
    ],
    "createdAt": "2024-12-01T10:00:00",
    "updatedAt": "2024-12-01T10:00:00"
  }
}
```

### 5.6 Update Order Status

**Endpoint:** `PATCH /api/v1/orders/{id}/status`

**Description:** Update order status

**Headers:**

```
Authorization: Bearer <token>
```

**Request Body:**

```json
{
  "status": "CONFIRMED"
}
```

**Valid Status Values:**

- `PENDING`
- `CONFIRMED`
- `PROCESSING`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Order status updated successfully",
  "data": {
    "id": 1,
    "orderNumber": "ORD-1638345600000",
    "customerId": 1,
    "totalAmount": 1999.98,
    "status": "CONFIRMED",
    "shippingAddress": {
      "fullName": "John Doe",
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    },
    "orderItems": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 999.99,
        "subtotal": 1999.98
      }
    ],
    "createdAt": "2024-12-01T10:00:00",
    "updatedAt": "2024-12-01T10:15:00"
  }
}
```

### 5.7 Cancel Order

**Endpoint:** `DELETE /api/v1/orders/{id}`

**Description:** Cancel an order (sets status to CANCELLED and releases stock)

**Headers:**

```
Authorization: Bearer <token>
```

**Response:** `200 OK`

```json
{
  "success": true,
  "code": 200,
  "message": "Order cancelled successfully",
  "data": null
}
```

---

## 6. File Service

Base Path: `/files` (internal service, not exposed through gateway for upload)

### 6.1 Upload File

**Endpoint:** `POST /files`

**Description:** Upload a file (used internally by product-service)

**Content-Type:** `multipart/form-data`

**Form Data:**

- `file` (file, required) - File to upload

**Response:** `200 OK`

```json
{
  "path": "uploads/1638345600000_image.jpg"
}
```

### 6.2 Delete File

**Endpoint:** `DELETE /files`

**Description:** Delete an uploaded file

**Request Body:**

```json
{
  "path": "uploads/1638345600000_image.jpg"
}
```

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "File deleted successfully"
}
```

### 6.3 Serve Uploaded Files

**Endpoint:** `GET /uploads/{filename}`

**Description:** Serve uploaded images (accessible through gateway)

**Example Request:**

```
GET http://localhost:8080/uploads/1638345600000_image.jpg
```

**Response:** `200 OK` (image file)

---

## Response Format

All API responses follow a consistent format:

### Success Response

```json
{
  "success": true,
  "code": 200,
  "message": "Operation successful",
  "data": { ... },
  "metadata": { ... }
}
```

### Error Response

```json
{
  "success": false,
  "code": 400,
  "message": "Error message",
  "data": null
}
```

### Pagination Metadata

```json
{
  "metadata": {
    "pagination": {
      "page": 0,
      "pageSize": 10,
      "totalPages": 5,
      "totalItems": 50
    }
  }
}
```

---

## Error Codes

| Code | Description           |
| ---- | --------------------- |
| 200  | Success               |
| 201  | Created               |
| 204  | No Content            |
| 400  | Bad Request           |
| 401  | Unauthorized          |
| 403  | Forbidden             |
| 404  | Not Found             |
| 500  | Internal Server Error |

---

## Authentication

Most endpoints require JWT authentication. Include the token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

Tokens are obtained from the `/api/v1/auth/login` or `/api/v1/auth/register` endpoints.

### Role-Based Access

- **Public Endpoints:** Register, Login
- **User Endpoints:** All product, cart, and order operations
- **Admin Endpoints:** User management (`/api/v1/users/**`)

---

## Service Ports (Internal)

| Service         | Port |
| --------------- | ---- |
| Gateway         | 8080 |
| Auth Service    | 8081 |
| Product Service | 8082 |
| Order Service   | 8083 |
| File Service    | 8084 |

**Note:** All client requests should go through the Gateway (port 8080). Internal ports are used for inter-service communication.
