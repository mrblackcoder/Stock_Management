# 📖 Complete Documentation - Stock Management System

## Table of Contents
1. [Architecture](#architecture)
2. [Database Design](#database-design)
3. [API Reference](#api-reference)
4. [Security](#security)
5. [Deployment](#deployment)
6. [Testing](#testing)

---

## Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT LAYER (Browser)                       │
│  ┌───────────────────────┐  ┌──────────────────────────────┐  │
│  │   React SPA           │  │  Thymeleaf Pages             │  │
│  │   (Port: 5173)        │  │  (login.html, register.html) │  │
│  │                       │  │                              │  │
│  │  - Dashboard          │  │  - Server-side rendered      │  │
│  │  - Products           │  │  - Bootstrap UI              │  │
│  │  - Transactions       │  │  - Form handling             │  │
│  └───────────────────────┘  └──────────────────────────────┘  │
└─────────────┬───────────────────────────┬──────────────────────┘
              │                           │
              │ REST API (JSON)           │ HTTP POST/GET
              │ JWT Token in Header       │
              │                           │
┌─────────────▼───────────────────────────▼──────────────────────┐
│              APPLICATION LAYER (Spring Boot - Port 8080)        │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              SECURITY LAYER                              │  │
│  │  ┌────────────────────┐  ┌──────────────────────────┐   │  │
│  │  │ JWT Filter         │→ │ Spring Security Config   │   │  │
│  │  │ Token Validation   │  │ - CORS                   │   │  │
│  │  │ User Authentication│  │ - CSRF (disabled)        │   │  │
│  │  └────────────────────┘  │ - Authorization          │   │  │
│  │                          └──────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              CONTROLLER LAYER (REST API)                 │  │
│  │  ┌────────────────┐ ┌────────────┐ ┌──────────────────┐ │  │
│  │  │ AuthController │ │ProductCtrl │ │TransactionCtrl   │ │  │
│  │  │ UserController │ │CategoryCtrl│ │ SupplierCtrl     │ │  │
│  │  └────────────────┘ └────────────┘ └──────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              SERVICE LAYER (Business Logic)              │  │
│  │  ┌────────────────┐ ┌────────────┐ ┌──────────────────┐ │  │
│  │  │ AuthService    │ │ProductSvc  │ │TransactionSvc    │ │  │
│  │  │ UserService    │ │CategorySvc │ │ SupplierSvc      │ │  │
│  │  │ JwtService     │ └────────────┘ └──────────────────┘ │  │
│  │  └────────────────┘                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │          REPOSITORY LAYER (Data Access - JPA)            │  │
│  │  ┌────────────────┐ ┌────────────┐ ┌──────────────────┐ │  │
│  │  │ UserRepo       │ │ProductRepo │ │TransactionRepo   │ │  │
│  │  │ (JpaRepo)      │ │(JpaRepo)   │ │(JpaRepo)         │ │  │
│  │  └────────────────┘ └────────────┘ └──────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              ENTITY LAYER (Domain Models)                │  │
│  │  ┌────────────────┐ ┌────────────┐ ┌──────────────────┐ │  │
│  │  │ User           │ │ Product    │ │ StockTransaction │ │  │
│  │  │ Category       │ │ Supplier   │ │                  │ │  │
│  │  └────────────────┘ └────────────┘ └──────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          │ JDBC (JPA/Hibernate)
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                   DATA LAYER (MySQL Database)                   │
│  ┌──────────┐ ┌──────────┐ ┌────────────┐ ┌──────────────────┐│
│  │  users   │ │ products │ │ categories │ │stock_transactions││
│  │ table    │ │ table    │ │ table      │ │ table            ││
│  └──────────┘ └──────────┘ └────────────┘ └──────────────────┘│
│  ┌──────────┐                                                  │
│  │suppliers │                                                  │
│  │ table    │                                                  │
│  └──────────┘                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Request Flow Example: Creating a Product

```
1. USER ACTION
   User fills product form in React → Clicks "Create Product"

2. FRONTEND (React)
   ProductPage.js → handleSubmit()
   ↓
   ApiService.createProduct(productData)
   ↓
   axios.post('/api/products', productData, {
     headers: { 'Authorization': 'Bearer ' + encryptedToken }
   })

3. NETWORK
   HTTP POST Request
   URL: http://localhost:8080/api/products
   Headers: Authorization, Content-Type
   Body: {"name": "Laptop", "price": 1500, ...}

4. BACKEND - Security Filter
   JwtAuthenticationFilter.doFilterInternal()
   ↓
   Extract JWT from header → Validate token
   ↓
   Load user details → Set SecurityContext
   
5. BACKEND - Controller
   ProductController.createProduct(@RequestBody ProductDTO dto)
   ↓
   Extract user from SecurityContext
   ↓
   Call service layer

6. BACKEND - Service
   ProductService.createProduct(dto)
   ↓
   Validation: Check if SKU exists
   ↓
   Business Logic: Set createdBy user
   ↓
   Convert DTO to Entity (ModelMapper)
   ↓
   Call repository

7. BACKEND - Repository
   ProductRepository.save(product)
   ↓
   JPA/Hibernate generates SQL
   ↓
   Execute INSERT statement

8. DATABASE
   MySQL executes:
   INSERT INTO products (name, sku, price, category_id, ...)
   VALUES ('Laptop', 'LAP-001', 1500, 1, ...)
   ↓
   Return generated ID

9. BACKEND - Response
   Repository → Service → Controller
   ↓
   Build Response object
   ↓
   Return ResponseEntity(201, response)

10. NETWORK
    HTTP 201 Created
    Body: {"statusCode": 201, "message": "Product created", ...}

11. FRONTEND
    axios receives response
    ↓
    Update React state (setProducts)
    ↓
    Re-render component
    ↓
    User sees new product in table
```

---

## Database Design

### Entity Relationship Diagram (Detailed)

```
┌─────────────────────────────────────────────────────────┐
│                      users                              │
│─────────────────────────────────────────────────────────│
│ id                BIGINT      PRIMARY KEY AUTO_INCREMENT│
│ username          VARCHAR(50) UNIQUE NOT NULL           │
│ email             VARCHAR(100) UNIQUE NOT NULL          │
│ password          VARCHAR(255) NOT NULL (BCrypt hashed) │
│ full_name         VARCHAR(100)                          │
│ role              VARCHAR(20)  NOT NULL (ADMIN/USER)    │
│ enabled           BOOLEAN      DEFAULT TRUE             │
│ created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP│
│ updated_at        TIMESTAMP    ON UPDATE CURRENT        │
└─────────────────────────────────────────────────────────┘
           │                                │
           │ created_by                     │ user_id
           │ (One-to-Many)                  │ (One-to-Many)
           │                                │
           ▼                                ▼
┌────────────────────────┐      ┌────────────────────────────────┐
│      products          │      │    stock_transactions          │
│────────────────────────│      │────────────────────────────────│
│ id            PK       │◄─────│ product_id      FK NOT NULL    │
│ name          NOT NULL │      │ id              PK             │
│ sku           UNIQUE   │      │ user_id         FK NOT NULL    │
│ price         DECIMAL  │      │ transaction_type VARCHAR(20)   │
│ stock_quantity INT     │      │   (PURCHASE/SALE/ADJUSTMENT)   │
│ reorder_level  INT     │      │ quantity        INT NOT NULL   │
│ category_id   FK       │      │ unit_price      DECIMAL        │
│ supplier_id   FK       │      │ status          VARCHAR(20)    │
│ created_by    FK       │      │   (PENDING/COMPLETED/CANCELLED)│
│ created_at    TIMESTAMP│      │ transaction_date TIMESTAMP     │
│ updated_at    TIMESTAMP│      │ notes           TEXT           │
└────────────────────────┘      └────────────────────────────────┘
    │              │
    │              │
    │ category_id  │ supplier_id
    │ (Many-to-One)│ (Many-to-One)
    │              │
    ▼              ▼
┌──────────────┐  ┌──────────────┐
│ categories   │  │  suppliers   │
│──────────────│  │──────────────│
│ id       PK  │  │ id       PK  │
│ name     UQ  │  │ name         │
│ description  │  │ email    UQ  │
│ created_at   │  │ phone        │
│ updated_at   │  │ address      │
└──────────────┘  │ contact_person│
                  │ created_at   │
                  │ updated_at   │
                  └──────────────┘
```

### Database Constraints

**Primary Keys:**
- All tables use `BIGINT AUTO_INCREMENT` for IDs

**Unique Constraints:**
- `users.username`
- `users.email`
- `products.sku`
- `categories.name`
- `suppliers.email`

**Foreign Key Constraints:**
```sql
ALTER TABLE products
  ADD CONSTRAINT fk_product_category 
    FOREIGN KEY (category_id) REFERENCES categories(id),
  ADD CONSTRAINT fk_product_supplier 
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
  ADD CONSTRAINT fk_product_created_by 
    FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE stock_transactions
  ADD CONSTRAINT fk_transaction_product 
    FOREIGN KEY (product_id) REFERENCES products(id),
  ADD CONSTRAINT fk_transaction_user 
    FOREIGN KEY (user_id) REFERENCES users(id);
```

**Indexes:**
```sql
-- For faster searches
CREATE INDEX idx_product_name ON products(name);
CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_transaction_date ON stock_transactions(transaction_date);
CREATE INDEX idx_transaction_type ON stock_transactions(transaction_type);
```

### Sample Data

```sql
-- Admin User
INSERT INTO users (username, email, password, full_name, role, enabled) 
VALUES ('admin', 'admin@local', '$2a$10$...bcrypt...', 'System Admin', 'ADMIN', true);

-- Categories
INSERT INTO categories (name, description) VALUES 
('Electronics', 'Electronic devices and accessories'),
('Office Supplies', 'Office equipment and supplies'),
('Furniture', 'Office and home furniture');

-- Suppliers
INSERT INTO suppliers (name, email, phone, address) VALUES 
('TechSupply Inc', 'contact@techsupply.com', '+1-555-0100', '123 Tech Street'),
('Office World', 'sales@officeworld.com', '+1-555-0200', '456 Supply Ave');

-- Products
INSERT INTO products (name, sku, price, stock_quantity, reorder_level, category_id, supplier_id, created_by) 
VALUES 
('Laptop Dell XPS 15', 'LAP-001', 1500.00, 50, 10, 1, 1, 1),
('Office Chair Ergonomic', 'CHR-001', 350.00, 25, 5, 3, 2, 1);

-- Transactions
INSERT INTO stock_transactions (product_id, user_id, transaction_type, quantity, unit_price, status) 
VALUES 
(1, 1, 'PURCHASE', 100, 1400.00, 'COMPLETED'),
(1, 1, 'SALE', 10, 1500.00, 'COMPLETED');
```

---

## API Reference

### Authentication

#### POST /api/auth/register
Register a new user account.

**Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe"
}
```

**Success Response (201):**
```json
{
  "statusCode": 201,
  "message": "User registered successfully",
  "user": {
    "id": 2,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "USER"
  }
}
```

**Error Response (400):**
```json
{
  "statusCode": 400,
  "message": "Username already exists"
}
```

#### POST /api/auth/login
Authenticate user and receive JWT token.

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Success Response (200):**
```json
{
  "statusCode": 200,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYxNjIzOTAyMiwiZXhwIjoxNjE2MzI1NDIyfQ.4X8WfNvXj-IKl3I7J9Z2K8VQO-Fm3YXwJ6Q3fXY8Z1Y",
  "expirationTime": "24Hr",
  "role": "ADMIN",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@local",
    "role": "ADMIN"
  }
}
```

### Products

#### GET /api/products
Get all products with pagination and sorting.

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Items per page
- `sortBy` (default: "createdAt") - Sort field

**Request:**
```
GET /api/products?page=0&size=10&sortBy=name
```

**Success Response (200):**
```json
{
  "statusCode": 200,
  "message": "Products retrieved successfully",
  "productList": [
    {
      "id": 1,
      "name": "Laptop Dell XPS 15",
      "sku": "LAP-001",
      "price": 1500.00,
      "stockQuantity": 50,
      "reorderLevel": 10,
      "category": {
        "id": 1,
        "name": "Electronics"
      },
      "supplier": {
        "id": 1,
        "name": "TechSupply Inc"
      },
      "createdAt": "2024-12-01T10:30:00",
      "updatedAt": "2024-12-01T10:30:00"
    }
  ],
  "totalPages": 1,
  "totalElements": 1
}
```

#### POST /api/products
Create a new product.

**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request:**
```json
{
  "name": "Laptop Dell XPS 15",
  "sku": "LAP-001",
  "price": 1500.00,
  "stockQuantity": 50,
  "reorderLevel": 10,
  "categoryId": 1,
  "supplierId": 1
}
```

**Success Response (201):**
```json
{
  "statusCode": 201,
  "message": "Product created successfully",
  "product": {
    "id": 1,
    "name": "Laptop Dell XPS 15",
    "sku": "LAP-001",
    "price": 1500.00,
    "stockQuantity": 50,
    "reorderLevel": 10
  }
}
```

#### PUT /api/products/{id}
Update existing product.

**Request:**
```json
{
  "name": "Laptop Dell XPS 15 (Updated)",
  "price": 1600.00,
  "stockQuantity": 55
}
```

#### DELETE /api/products/{id}
Delete a product (Admin only or owner).

**Success Response (200):**
```json
{
  "statusCode": 200,
  "message": "Product deleted successfully"
}
```

#### GET /api/products/low-stock
Get products below reorder level.

**Success Response (200):**
```json
{
  "statusCode": 200,
  "message": "Low stock products retrieved",
  "productList": [
    {
      "id": 5,
      "name": "Mouse Wireless",
      "stockQuantity": 5,
      "reorderLevel": 10,
      "difference": -5
    }
  ]
}
```

### Categories

#### GET /api/categories
Get all categories.

**Success Response (200):**
```json
{
  "statusCode": 200,
  "message": "Categories retrieved successfully",
  "categoryList": [
    {
      "id": 1,
      "name": "Electronics",
      "description": "Electronic devices",
      "productCount": 15
    }
  ]
}
```

#### POST /api/categories
Create new category.

**Request:**
```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```

### Suppliers

#### GET /api/suppliers
Get all suppliers.

**Success Response (200):**
```json
{
  "statusCode": 200,
  "message": "Suppliers retrieved successfully",
  "supplierList": [
    {
      "id": 1,
      "name": "TechSupply Inc",
      "email": "contact@techsupply.com",
      "phone": "+1-555-0100",
      "address": "123 Tech Street",
      "productCount": 25
    }
  ]
}
```

#### POST /api/suppliers
Create new supplier.

**Request:**
```json
{
  "name": "TechSupply Inc",
  "email": "contact@techsupply.com",
  "phone": "+1-555-0100",
  "address": "123 Tech Street, NY",
  "contactPerson": "John Smith"
}
```

### Stock Transactions

#### POST /api/transactions
Create a new stock transaction.

**Request:**
```json
{
  "productId": 1,
  "transactionType": "PURCHASE",
  "quantity": 100,
  "unitPrice": 1400.00,
  "notes": "Monthly stock replenishment"
}
```

**Transaction Types:**
- `PURCHASE` - Adds to stock
- `SALE` - Reduces stock
- `ADJUSTMENT` - Manual correction

**Success Response (201):**
```json
{
  "statusCode": 201,
  "message": "Transaction created successfully",
  "transaction": {
    "id": 1,
    "product": {
      "id": 1,
      "name": "Laptop Dell XPS 15"
    },
    "transactionType": "PURCHASE",
    "quantity": 100,
    "unitPrice": 1400.00,
    "totalAmount": 140000.00,
    "status": "COMPLETED",
    "transactionDate": "2024-12-01T14:30:00"
  },
  "updatedStock": 150
}
```

---

## Security

### Authentication & Authorization

#### JWT Token Flow

```
1. LOGIN REQUEST
   POST /api/auth/login
   Body: { username, password }
   
2. BACKEND VALIDATION
   AuthService.login()
   ↓
   Load user from database
   ↓
   Compare BCrypt hash: passwordEncoder.matches(raw, encoded)
   ↓
   If valid, generate JWT token

3. JWT TOKEN GENERATION
   JwtService.generateToken(userDetails)
   ↓
   Header: { "alg": "HS256", "typ": "JWT" }
   Payload: { 
     "sub": "admin", 
     "iat": 1616239022,
     "exp": 1616325422,
     "role": "ADMIN"
   }
   Signature: HMACSHA256(header + payload, SECRET_KEY)
   ↓
   Return encoded token

4. CLIENT STORAGE
   Frontend receives token
   ↓
   Encrypt with CryptoJS (AES-256)
   ↓
   Store in localStorage

5. SUBSEQUENT REQUESTS
   Client retrieves token from localStorage
   ↓
   Decrypt token
   ↓
   Add to header: Authorization: Bearer {token}

6. BACKEND VALIDATION
   JwtAuthenticationFilter intercepts request
   ↓
   Extract token from header
   ↓
   JwtService.validateToken()
   ↓
   Check signature, expiration, user existence
   ↓
   If valid, set SecurityContext
   ↓
   Request proceeds to controller
```

#### Password Security

**BCrypt Implementation:**
```java
// Registration
String rawPassword = "admin123";
String encodedPassword = passwordEncoder.encode(rawPassword);
// Output: $2a$10$N9qo8uLOickgx2ZMRZoMy.bIZNC2jXnysd7K4YxKOJqQ7oLG7x3Iy

// Login validation
boolean matches = passwordEncoder.matches("admin123", encodedPassword);
// BCrypt automatically handles salt and hashing
```

**Password Requirements:**
- Minimum length: 6 characters (should be 8+ in production)
- Hashed with BCrypt (cost factor 10)
- Salted automatically by BCrypt
- Never stored in plain text

#### Role-Based Access Control

**User Roles:**
```java
public enum UserRole {
    ADMIN,  // Full access to all operations
    USER    // Limited access (can't delete others' products)
}
```

**Authorization Rules:**

| Endpoint | Anonymous | USER | ADMIN |
|----------|-----------|------|-------|
| POST /api/auth/register | ✅ | ✅ | ✅ |
| POST /api/auth/login | ✅ | ✅ | ✅ |
| GET /api/products | ❌ | ✅ | ✅ |
| POST /api/products | ❌ | ✅ | ✅ |
| DELETE /api/products/{id} (own) | ❌ | ✅ | ✅ |
| DELETE /api/products/{id} (any) | ❌ | ❌ | ✅ |
| GET /api/users/all | ❌ | ❌ | ✅ |

**Implementation:**
```java
// Service layer
public Response deleteProduct(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Product not found"));
    
    User currentUser = getCurrentUser();
    boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
    boolean isOwner = product.getCreatedBy().getId().equals(currentUser.getId());
    
    if (!isAdmin && !isOwner) {
        throw new SecurityException("You can only delete products you created");
    }
    
    productRepository.delete(product);
}
```

### Security Threats & Mitigations

#### 1. SQL Injection

**Vulnerability:**
```java
// DANGEROUS - Never do this!
String query = "SELECT * FROM users WHERE username = '" + username + "'";
// Attacker input: admin' OR '1'='1
// Resulting query: SELECT * FROM users WHERE username = 'admin' OR '1'='1'
```

**Protection (JPA):**
```java
// SAFE - JPA uses prepared statements
@Query("SELECT u FROM User u WHERE u.username = :username")
Optional<User> findByUsername(@Param("username") String username);

// SAFE - Method name queries
Optional<User> findByUsername(String username);
```

#### 2. XSS (Cross-Site Scripting)

**Vulnerability:**
```html
<!-- DANGEROUS - User input rendered as HTML -->
<div th:utext="${userComment}"></div>
<!-- If userComment = "<script>alert('XSS')</script>", it executes! -->
```

**Protection (Thymeleaf):**
```html
<!-- SAFE - Auto-escaping -->
<div th:text="${userComment}"></div>
<!-- Output: &lt;script&gt;alert('XSS')&lt;/script&gt; -->
```

#### 3. CSRF (Cross-Site Request Forgery)

**Note:** CSRF protection is disabled for stateless JWT API. 
For Thymeleaf forms, it's automatically enabled by Spring Security.

```html
<!-- Spring Security auto-includes CSRF token -->
<form th:action="@{/login}" method="post">
    <input type="hidden" th:name="${_csrf.parameterName}" 
           th:value="${_csrf.token}" />
</form>
```

#### 4. Brute Force Attacks

**TODO:** Implement rate limiting (see enhancement section)

```java
@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    
    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0);
        attemptsCache.put(username, attempts + 1);
    }
    
    public boolean isBlocked(String username) {
        return attemptsCache.getOrDefault(username, 0) >= MAX_ATTEMPTS;
    }
}
```

### Security Headers

**Configured by Spring Security:**
```http
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
```

**CORS Configuration:**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList(
        "http://localhost:*", "http://127.0.0.1:*"
    ));
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

---

## Deployment

### AWS Deployment Guide

#### Prerequisites
1. AWS Account
2. AWS CLI installed and configured
3. Domain name (optional, for custom domain)

#### Architecture on AWS

```
┌─────────────────────────────────────────────────────────────┐
│                     Route 53 (DNS)                          │
│                  yourdomain.com                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              CloudFront (CDN)                                │
│          SSL Certificate (HTTPS)                            │
└────────┬────────────────────────────┬───────────────────────┘
         │                            │
         │ /api/*                     │ /*
         │                            │
         ▼                            ▼
┌─────────────────────┐    ┌──────────────────────────────────┐
│ Elastic Beanstalk   │    │  S3 Bucket (Static Website)      │
│ Spring Boot App     │    │  React Build Files               │
│ (Auto-scaling)      │    │  - index.html                    │
│                     │    │  - static/js/                    │
│ ┌─────────────────┐ │    │  - static/css/                   │
│ │ EC2 Instances   │ │    └──────────────────────────────────┘
│ │ (t2.micro/small)│ │
│ └─────────────────┘ │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────────────┐
│      RDS MySQL Database             │
│      (Multi-AZ for HA)              │
│                                     │
│  Primary: us-east-1a                │
│  Standby: us-east-1b                │
└─────────────────────────────────────┘
```

#### Step 1: Database Setup (RDS)

1. **Create RDS MySQL Instance:**
```bash
aws rds create-db-instance \
    --db-instance-identifier inventory-db \
    --db-instance-class db.t3.micro \
    --engine mysql \
    --engine-version 8.0.35 \
    --master-username admin \
    --master-user-password YourSecurePassword123 \
    --allocated-storage 20 \
    --vpc-security-group-ids sg-xxxxxx \
    --db-name inventory_management_db \
    --publicly-accessible
```

2. **Update application.properties:**
```properties
spring.datasource.url=jdbc:mysql://inventory-db.xxxxxx.us-east-1.rds.amazonaws.com:3306/inventory_management_db
spring.datasource.username=admin
spring.datasource.password=YourSecurePassword123
```

#### Step 2: Backend Deployment (Elastic Beanstalk)

1. **Build JAR file:**
```bash
./gradlew clean build -x test
```

2. **Create Elastic Beanstalk application:**
```bash
# Initialize EB
eb init -p "Corretto 21" inventory-management-api --region us-east-1

# Create environment
eb create inventory-api-env \
    --instance-type t2.small \
    --envvars \
        JWT_SECRET="your-production-secret-key-change-this" \
        SPRING_DATASOURCE_URL="jdbc:mysql://your-rds-endpoint:3306/inventory_management_db" \
        SPRING_DATASOURCE_USERNAME="admin" \
        SPRING_DATASOURCE_PASSWORD="YourSecurePassword123"

# Deploy
eb deploy
```

3. **Configure environment variables:**
```bash
eb setenv \
    JWT_SECRET="production-secret-key" \
    JWT_EXPIRATION="86400000" \
    SPRING_PROFILES_ACTIVE="production"
```

#### Step 3: Frontend Deployment (S3 + CloudFront)

1. **Build React app:**
```bash
cd frontend

# Update API endpoint
# Edit .env.production
echo "VITE_API_URL=https://api.yourdomain.com/api" > .env.production

# Build
npm run build
```

2. **Create S3 bucket:**
```bash
aws s3 mb s3://inventory-management-frontend

# Enable static website hosting
aws s3 website s3://inventory-management-frontend \
    --index-document index.html \
    --error-document index.html

# Upload build files
aws s3 sync dist/ s3://inventory-management-frontend/
```

3. **Create CloudFront distribution:**
```bash
aws cloudfront create-distribution \
    --origin-domain-name inventory-management-frontend.s3.amazonaws.com \
    --default-root-object index.html
```

4. **Configure CORS on backend:**
```properties
# Update allowed origins for production
cors.allowed-origins=https://your-cloudfront-domain.cloudfront.net,https://yourdomain.com
```

#### Step 4: SSL Certificate (HTTPS)

1. **Request certificate in ACM:**
```bash
aws acm request-certificate \
    --domain-name yourdomain.com \
    --subject-alternative-names www.yourdomain.com api.yourdomain.com \
    --validation-method DNS
```

2. **Validate domain ownership:**
- Add CNAME records provided by ACM to your DNS

3. **Attach to CloudFront and Load Balancer**

#### Step 5: Environment Variables (Production)

**Elastic Beanstalk:**
```properties
# Never commit these to Git!
JWT_SECRET=super-secret-production-key-min-256-bits
JWT_EXPIRATION=86400000
SPRING_DATASOURCE_URL=jdbc:mysql://rds-endpoint:3306/db
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=SecurePass123
SPRING_PROFILES_ACTIVE=production
```

#### Cost Estimate (Monthly)

| Service | Configuration | Estimated Cost |
|---------|---------------|----------------|
| RDS MySQL | db.t3.micro (20GB) | $15 |
| Elastic Beanstalk | t2.small (1 instance) | $17 |
| S3 | Static files (~100MB) | $0.23 |
| CloudFront | 1GB transfer | $0.085 |
| Route 53 | 1 hosted zone | $0.50 |
| **Total** | | **~$33/month** |

**Free Tier Eligible:**
- RDS: 750 hours/month (t3.micro)
- EC2: 750 hours/month (t2.micro)
- S3: 5GB storage
- CloudFront: 1TB transfer (12 months)

---

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests ProductServiceTest
```

### Manual API Testing

**Using cURL:**
```bash
# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"test123","fullName":"Test User"}'

# Login
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

# Get products
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"

# Create product
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Product","sku":"TEST-001","price":100,"stockQuantity":50,"reorderLevel":10,"categoryId":1,"supplierId":1}'
```

### Frontend Testing

```bash
cd frontend

# Run development server
npm run dev

# Test production build locally
npm run build
npm run preview
```

---

**Document Version:** 1.0.0  
**Last Updated:** December 2024  
**Maintained by:** Mehmet Taha Boynikoğlu
