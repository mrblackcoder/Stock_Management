# 📖 API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
Bu API JWT (JSON Web Token) kullanır. Korumalı endpoint'lere erişmek için Authorization header'ı gereklidir:
```
Authorization: Bearer {your_token_here}
```

---

## 🔐 Authentication Endpoints

### Register New User
Yeni kullanıcı kaydı oluşturur.

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "USER" | "ADMIN"
}
```

**Success Response:** `201 Created`
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "newuser",
    "email": "user@example.com",
    "role": "USER"
  }
}
```

**Error Response:** `400 Bad Request`
```json
{
  "success": false,
  "message": "Username already exists",
  "data": null
}
```

---

### Login
Mevcut kullanıcı girişi yapar ve JWT token döner.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "admin",
    "role": "ADMIN"
  }
}
```

**Error Response:** `401 Unauthorized`
```json
{
  "success": false,
  "message": "Invalid credentials",
  "data": null
}
```

---

## 📦 Product Endpoints

### Get All Products
Tüm ürünleri listeler.

**Endpoint:** `GET /api/products`

**Headers:** `Authorization: Bearer {token}`

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Laptop",
      "description": "High performance laptop",
      "sku": "LAP-001",
      "price": 15000.00,
      "quantity": 10,
      "categoryId": 1,
      "categoryName": "Electronics",
      "supplierId": 1,
      "supplierName": "Tech Supplier Inc.",
      "createdAt": "2024-11-20T10:30:00"
    }
  ]
}
```

---

### Get Product by ID
Belirli bir ürünü ID'ye göre getirir.

**Endpoint:** `GET /api/products/{id}`

**Headers:** `Authorization: Bearer {token}`

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Product found",
  "data": {
    "id": 1,
    "name": "Laptop",
    "description": "High performance laptop",
    "sku": "LAP-001",
    "price": 15000.00,
    "quantity": 10,
    "categoryId": 1,
    "categoryName": "Electronics",
    "supplierId": 1,
    "supplierName": "Tech Supplier Inc.",
    "createdAt": "2024-11-20T10:30:00"
  }
}
```

**Error Response:** `404 Not Found`
```json
{
  "success": false,
  "message": "Product not found with id: 1",
  "data": null
}
```

---

### Create Product
Yeni ürün oluşturur. (ADMIN only)

**Endpoint:** `POST /api/products`

**Headers:** 
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Laptop",
  "description": "High performance laptop",
  "sku": "LAP-001",
  "price": 15000.00,
  "quantity": 10,
  "categoryId": 1,
  "supplierId": 1
}
```

**Success Response:** `201 Created`
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "Laptop",
    "sku": "LAP-001",
    "price": 15000.00,
    "quantity": 10
  }
}
```

**Error Response:** `400 Bad Request`
```json
{
  "success": false,
  "message": "Product with SKU LAP-001 already exists",
  "data": null
}
```

---

### Update Product
Mevcut ürünü günceller. (ADMIN only)

**Endpoint:** `PUT /api/products/{id}`

**Headers:** 
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Updated Laptop",
  "description": "Updated description",
  "sku": "LAP-001",
  "price": 16000.00,
  "quantity": 15,
  "categoryId": 1,
  "supplierId": 1
}
```

**Success Response:** `200 OK`

---

### Delete Product
Ürünü siler. (ADMIN only)

**Endpoint:** `DELETE /api/products/{id}`

**Headers:** `Authorization: Bearer {token}`

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Product deleted successfully",
  "data": null
}
```

---

### Search Products
Ürün adı veya SKU'ya göre arama yapar.

**Endpoint:** `GET /api/products/search?keyword={keyword}`

**Headers:** `Authorization: Bearer {token}`

**Query Parameters:**
- `keyword` (required): Arama kelimesi

**Success Response:** `200 OK`

---

### Get Low Stock Products
Düşük stoklu ürünleri listeler.

**Endpoint:** `GET /api/products/low-stock`

**Headers:** `Authorization: Bearer {token}`

**Success Response:** `200 OK`

---

## 📂 Category Endpoints

### Get All Categories
Tüm kategorileri listeler.

**Endpoint:** `GET /api/categories`

**Headers:** `Authorization: Bearer {token}`

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Electronics",
      "description": "Electronic devices and accessories"
    }
  ]
}
```

---

### Create Category
Yeni kategori oluşturur. (ADMIN only)

**Endpoint:** `POST /api/categories`

**Headers:** 
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```

**Success Response:** `201 Created`

---

### Update Category
Kategori günceller. (ADMIN only)

**Endpoint:** `PUT /api/categories/{id}`

**Headers:** 
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

---

### Delete Category
Kategori siler. (ADMIN only)

**Endpoint:** `DELETE /api/categories/{id}`

**Headers:** `Authorization: Bearer {token}`

**Success Response:** `200 OK`

---

## 🏢 Supplier Endpoints

### Get All Suppliers
Tüm tedarikçileri listeler.

**Endpoint:** `GET /api/suppliers`

**Headers:** `Authorization: Bearer {token}`

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Suppliers retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Tech Supplier Inc.",
      "contactPerson": "John Doe",
      "phone": "+90 555 123 4567",
      "email": "contact@techsupplier.com",
      "address": "Istanbul, Turkey"
    }
  ]
}
```

---

### Create Supplier
Yeni tedarikçi oluşturur. (ADMIN only)

**Endpoint:** `POST /api/suppliers`

**Headers:** 
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Tech Supplier Inc.",
  "contactPerson": "John Doe",
  "phone": "+90 555 123 4567",
  "email": "contact@techsupplier.com",
  "address": "Istanbul, Turkey"
}
```

**Success Response:** `201 Created`

---

### Update Supplier
Tedarikçi günceller. (ADMIN only)

**Endpoint:** `PUT /api/suppliers/{id}`

---

### Delete Supplier
Tedarikçi siler. (ADMIN only)

**Endpoint:** `DELETE /api/suppliers/{id}`

---

## 📊 Transaction Endpoints

### Get All Transactions
Tüm stok işlemlerini listeler.

**Endpoint:** `GET /api/transactions`

**Headers:** `Authorization: Bearer {token}`

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "Transactions retrieved successfully",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop",
      "userId": 1,
      "username": "admin",
      "transactionType": "PURCHASE",
      "quantity": 50,
      "notes": "Yeni stok girişi",
      "transactionDate": "2024-11-20T10:30:00"
    }
  ]
}
```

---

### Create Transaction
Yeni stok işlemi oluşturur.

**Endpoint:** `POST /api/transactions`

**Headers:** 
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "productId": 1,
  "transactionType": "PURCHASE" | "SALE" | "ADJUSTMENT",
  "quantity": 50,
  "notes": "Yeni stok girişi"
}
```

**Transaction Types:**
- `PURCHASE`: Alış işlemi (stok artar)
- `SALE`: Satış işlemi (stok azalır)
- `ADJUSTMENT`: Düzeltme işlemi

**Success Response:** `201 Created`

---

### Get Transactions by Product
Belirli bir ürüne ait işlemleri getirir.

**Endpoint:** `GET /api/transactions/product/{productId}`

**Headers:** `Authorization: Bearer {token}`

---

### Get Transactions by User
Belirli bir kullanıcıya ait işlemleri getirir.

**Endpoint:** `GET /api/transactions/user/{userId}`

**Headers:** `Authorization: Bearer {token}`

---

## 👤 User Endpoints

### Get Current User Profile
Giriş yapmış kullanıcının profil bilgilerini getirir.

**Endpoint:** `GET /api/users/profile`

**Headers:** `Authorization: Bearer {token}`

**Success Response:** `200 OK`
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "role": "ADMIN",
    "createdAt": "2024-11-20T10:30:00"
  }
}
```

---

### Update User Profile
Kullanıcı profil bilgilerini günceller.

**Endpoint:** `PUT /api/users/profile`

**Headers:** 
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "email": "newemail@example.com",
  "password": "newpassword123"
}
```

---

## 🔍 Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | OK - İstek başarılı |
| 201 | Created - Kaynak başarıyla oluşturuldu |
| 400 | Bad Request - Geçersiz istek |
| 401 | Unauthorized - Kimlik doğrulama gerekli |
| 403 | Forbidden - Erişim izni yok |
| 404 | Not Found - Kaynak bulunamadı |
| 409 | Conflict - Kaynak çakışması (örn: duplicate entry) |
| 500 | Internal Server Error - Sunucu hatası |

---

## 📝 Response Format

Tüm API yanıtları aşağıdaki formatta döner:

```json
{
  "success": boolean,
  "message": "string",
  "data": object | array | null
}
```

- `success`: İsteğin başarılı olup olmadığı
- `message`: İnsan tarafından okunabilir mesaj
- `data`: Dönen veri (yoksa null)

---

## 🔒 JWT Token Format

Token format:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsInJvbGUiOiJBRE1JTiIsImV4cCI6MTYzODM2MDAwMH0.signature
```

Token içeriği:
```json
{
  "sub": "username",
  "role": "ADMIN",
  "iat": 1638360000,
  "exp": 1638446400
}
```

Token geçerlilik süresi: **24 saat**

---

## 🧪 Testing with cURL

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "test123",
    "role": "USER"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Get Products
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "sku": "TEST-001",
    "price": 100.00,
    "quantity": 10,
    "categoryId": 1,
    "supplierId": 1
  }'
```

---

**Last Updated:** 2024-11-20
**API Version:** 1.0.0

