# Product Service API Documentation

Base URL: `http://localhost:8081`

## Endpoints

### 1. Get All Products
- **URL:** `/api/products`
- **Method:** `GET`
- **Description:** Retrieves a list of all products.
- **Response:** `200 OK` (List of ProductDTO)

### 2. Get Product by ID
- **URL:** `/api/products/{id}`
- **Method:** `GET`
- **Path Parameters:**
    - `id`: Long (Product ID)
- **Description:** Retrieves a specific product by its ID.
- **Response:** `200 OK` (ProductDTO)

### 3. Search Products
- **URL:** `/api/products/search`
- **Method:** `GET`
- **Query Parameters:**
    - `name`: String (Name to search for)
- **Description:** Search for products by name.
- **Response:** `200 OK` (List of ProductDTO)

### 4. Get Products by Category
- **URL:** `/api/products/category/{category}`
- **Method:** `GET`
- **Path Parameters:**
    - `category`: String (Category name)
- **Description:** Retrieves products belonging to a specific category.
- **Response:** `200 OK` (List of ProductDTO)

### 5. Create Product
- **URL:** `/api/products`
- **Method:** `POST`
- **Body:**
```json
{
  "name": "Product Name",
  "description": "Product Description",
  "price": 99.99,
  "stockQuantity": 100,
  "category": "Electronics",
  "imageUrl": "http://example.com/image.jpg",
  "active": true
}
```
- **Description:** Creates a new product.
- **Response:** `201 Created` (ProductDTO)

### 6. Update Product
- **URL:** `/api/products/{id}`
- **Method:** `PUT`
- **Path Parameters:**
    - `id`: Long (Product ID)
- **Body:**
```json
{
  "name": "Updated Name",
  "description": "Updated Description",
  "price": 129.99,
  "stockQuantity": 50,
  "category": "Electronics",
  "imageUrl": "http://example.com/new-image.jpg",
  "active": true
}
```
- **Description:** Updates an existing product.
- **Response:** `200 OK` (ProductDTO)

### 7. Delete Product
- **URL:** `/api/products/{id}`
- **Method:** `DELETE`
- **Path Parameters:**
    - `id`: Long (Product ID)
- **Description:** Deletes a product by its ID.
- **Response:** `204 No Content`

### 8. Reserve Stock
- **URL:** `/api/products/{id}/reserve`
- **Method:** `POST`
- **Path Parameters:**
    - `id`: Long (Product ID)
- **Query Parameters:**
    - `quantity`: Integer (Amount to reserve)
- **Description:** Reserves a specific quantity of stock for a product.
- **Response:** `200 OK`
```json
{
  "success": true,
  "message": "Stock reserved successfully"
}
```

### 9. Release Stock
- **URL:** `/api/products/{id}/release`
- **Method:** `POST`
- **Path Parameters:**
    - `id`: Long (Product ID)
- **Query Parameters:**
    - `quantity`: Integer (Amount to release)
- **Description:** Releases a specific quantity of stock back to the product.
- **Response:** `200 OK`
