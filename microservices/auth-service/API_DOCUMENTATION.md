# Auth Service API Documentation

Base URL: `http://localhost:8081` (Adjust port if necessary)

## Endpoints

### 1. User Signup
Register a new user.

- **URL:** `/auth/signup`
- **Method:** `POST`
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "roles": "ROLE_USER"
}
```

**Response (200 OK):**
```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "roles": "ROLE_USER"
    }
  },
  "message": "User registered successfully"
}
```

### 2. User Login
Authenticate a user and receive a JWT token.

- **URL:** `/auth/login`
- **Method:** `POST`
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "username": "john.doe@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john.doe@example.com",
      "roles": "ROLE_USER"
    }
  },
  "message": "Login successful"
}
```

**Response (403 Forbidden):**
If authentication fails.

### 3. Get Current User Info
Retrieve information about the currently authenticated user.

- **URL:** `/auth/me`
- **Method:** `GET`
- **Headers:**
    - `Authorization`: `Bearer <your_jwt_token>`

**Response (200 OK):**
```json
{
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "roles": "ROLE_USER"
  },
  "message": "User information retrieved successfully"
}
```
