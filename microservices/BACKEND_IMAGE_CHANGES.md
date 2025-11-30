# Backend API Changes for Product Images

This document outlines the necessary backend changes to support main and secondary images for products.

## 1. Product Model Update

The `Product` model needs to be updated to replace the single `imageUrl` field with `mainImage` and `secondaryImages`.

- **`mainImage`**: This will store the URL for the primary product image.
- **`secondaryImages`**: This will be an array of URLs for additional product images.

### Prisma Schema Example

If you are using Prisma, the changes to your `Product` model would look like this:

```prisma
model Product {
  // ... other fields
  mainImage        String
  secondaryImages  String[]
  // imageUrl       String   @deprecated
}
```

## 2. Create Product Endpoint (`POST /api/v1/products`)

The create product endpoint should now accept a `mainImage` file and multiple `secondaryImages` files.

-   The request will be of type `multipart/form-data`.
-   `mainImage`: A single file upload.
-   `secondaryImages`: An array of files.

The backend should:
1.  Upload the `mainImage` and all `secondaryImages` to a storage service (like S3, Cloudinary, etc.).
2.  Store the resulting URLs in the `mainImage` and `secondaryImages` fields of the new product record in the database.

## 3. Update Product Endpoint (`PUT /api/v1/products/:id`)

The update product endpoint should also support updating `mainImage` and `secondaryImages`.

-   The request will be of type `multipart/form-data`.
-   If a new `mainImage` is provided, it should replace the old one.
-   If new `secondaryImages` are provided, you can either decide to replace all existing secondary images or append to them. A replacement strategy is often simpler to implement.
-   The backend should handle deleting the old images from the storage service to avoid orphaned files.

## 4. Product Response DTO

The `Product` data returned from all endpoints should include `mainImage` and `secondaryImages` instead of `imageUrl`.

Example response:

```json
{
  "id": 1,
  "name": "Cool Product",
  "description": "A very cool product.",
  "price": 99.99,
  "category": "Electronics",
  "stockQuantity": 100,
  "active": true,
  "rating": 4.5,
  "mainImage": "path/to/main-image.jpg",
  "secondaryImages": [
    "path/to/secondary-image-1.jpg",
    "path/to/secondary-image-2.jpg"
  ]
}
```
