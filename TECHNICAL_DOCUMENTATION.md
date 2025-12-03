# Technical Documentation
## Stock Management System - Teknik Dokümantasyon

**Proje:** Inventory Management System  
**Öğrenci:** Mehmet Taha Boynikoğlu (212 125 10 34)  
**Teknoloji Stack:** Spring Boot 3.5.7 + React 19.2.0  
**Tarih:** Aralık 2024

---

## İçindekiler

1. [Sistem Mimarisi](#sistem-mimarisi)
2. [Teknoloji Stack](#teknoloji-stack)
3. [Veritabanı Tasarımı](#veritabanı-tasarımı)
4. [Backend Mimarisi](#backend-mimarisi)
5. [Frontend Mimarisi](#frontend-mimarisi)
6. [API Dokümantasyonu](#api-dokümantasyonu)
7. [Güvenlik Mimarisi](#güvenlik-mimarisi)
8. [Deployment](#deployment)
9. [Testing](#testing)
10. [Performans Optimizasyonu](#performans-optimizasyonu)

---

## Sistem Mimarisi

### Genel Mimari

```
┌─────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Browser    │  │    Mobile    │  │   Desktop    │  │
│  │  (React.js)  │  │  (Future)    │  │  (Future)    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP/HTTPS
                      │ REST API
┌─────────────────────▼───────────────────────────────────┐
│                  APPLICATION LAYER                       │
│  ┌────────────────────────────────────────────────┐     │
│  │         Spring Boot Application                 │     │
│  │  ┌──────────────┐  ┌──────────────┐            │     │
│  │  │ Controllers  │  │   Services   │            │     │
│  │  └──────┬───────┘  └──────┬───────┘            │     │
│  │         │                 │                     │     │
│  │  ┌──────▼─────────────────▼───────┐            │     │
│  │  │     Business Logic Layer       │            │     │
│  │  └──────┬─────────────────────────┘            │     │
│  │         │                                       │     │
│  │  ┌──────▼───────────────────────────┐          │     │
│  │  │   Data Access Layer (JPA)        │          │     │
│  │  └──────┬───────────────────────────┘          │     │
│  └─────────┼────────────────────────────────────┘     │
└────────────┼──────────────────────────────────────────┘
             │ JDBC
┌────────────▼──────────────────────────────────────────┐
│                  DATA LAYER                            │
│  ┌────────────────────────────────────────────────┐   │
│  │            MySQL Database 8.0                   │   │
│  │  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐       │   │
│  │  │Users │  │Prods │  │Cats  │  │Supps │       │   │
│  │  └──────┘  └──────┘  └──────┘  └──────┘       │   │
│  └────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────┘
```

### Mimari Tasarım Prensipleri

**Layered Architecture (Katmanlı Mimari):**
- **Presentation Layer:** Controllers, DTOs
- **Business Layer:** Services, Business Logic
- **Persistence Layer:** Repositories, Entities
- **Cross-Cutting:** Security, Validation, Exception Handling

**Design Patterns Kullanımı:**
- **MVC Pattern:** Controller-Service-Repository
- **DTO Pattern:** Data Transfer Objects
- **Strategy Pattern:** Transaction strategies
- **Singleton Pattern:** Configuration beans
- **Factory Pattern:** ModelMapper configuration
- **Repository Pattern:** Data access abstraction

**SOLID Principles:**
- **S:** Single Responsibility - Her sınıf tek sorumluluk
- **O:** Open/Closed - Extension için açık, modification için kapalı
- **L:** Liskov Substitution - Alt sınıflar üst sınıf yerine kullanılabilir
- **I:** Interface Segregation - Küçük, spesifik interface'ler
- **D:** Dependency Inversion - Abstraction'a bağımlılık

---

## Teknoloji Stack

### Backend Technologies

**Core Framework:**
```
Spring Boot: 3.5.7
├── Spring Web (REST API)
├── Spring Security (Authentication & Authorization)
├── Spring Data JPA (ORM)
├── Spring Validation (Input Validation)
└── Spring Boot DevTools (Development)
```

**Database:**
```
MySQL: 8.0
├── InnoDB Engine
├── UTF-8 Character Set
└── ACID Compliance
```

**Security:**
```
JWT (JSON Web Tokens)
├── jjwt-api: 0.12.6
├── jjwt-impl: 0.12.6
└── jjwt-jackson: 0.12.6

Password Hashing:
└── BCrypt (Spring Security)
```

**Utilities:**
```
Lombok: Latest
├── @Data, @Builder annotations
├── Reduces boilerplate code
└── Compile-time code generation

ModelMapper: 3.1.1
├── DTO ↔ Entity conversion
└── Automated object mapping

SpringDoc OpenAPI: 2.1.0
├── API documentation
├── Swagger UI integration
└── Interactive API testing
```

**Build Tool:**
```
Gradle: 8.14.3
├── Groovy DSL
├── Dependency management
└── Multi-module support
```

### Frontend Technologies

**Core Library:**
```
React: 19.2.0
├── Functional Components
├── Hooks (useState, useEffect, useCallback)
├── Context API (Future)
└── React Router DOM: 7.1.1
```

**HTTP Client:**
```
Axios: 1.7.9
├── Promise-based HTTP client
├── Request/Response interceptors
├── Automatic JSON transformation
└── Error handling
```

**Styling:**
```
Bootstrap: 5.3.3
├── Responsive grid system
├── Pre-built components
└── Utility classes

React Bootstrap: 2.10.7
├── React components for Bootstrap
└── Enhanced Bootstrap integration

Custom CSS:
├── Component-specific stylesheets
└── Responsive design
```

**Security:**
```
crypto-js: 4.2.0
├── Client-side encryption
└── Token storage security
```

**Testing:**
```
Jest: Latest
├── Unit testing
└── Component testing

React Testing Library: Latest
├── DOM testing
└── User interaction testing
```

**Build Tool:**
```
npm: 10+
├── Package management
├── Script automation
└── Dependency resolution
```

### Development Tools

**Version Control:**
```
Git
└── GitHub (mrblackcoder/Stock_Management)
```

**IDE/Editors:**
```
IntelliJ IDEA (Backend)
VS Code (Frontend)
```

**API Testing:**
```
Swagger UI: http://localhost:8080/swagger-ui.html
Postman (Optional)
```

**Database Management:**
```
MySQL Workbench
DBeaver
phpMyAdmin
```

---

## Veritabanı Tasarımı

### Entity Relationship Diagram (Text)

```
┌─────────────────┐         ┌─────────────────┐
│     USERS       │         │  REFRESH_TOKENS │
├─────────────────┤         ├─────────────────┤
│ id (PK)         │◄───────┤ id (PK)         │
│ username (UQ)   │    1:1  │ token (UQ)      │
│ password        │         │ user_id (FK,UQ) │
│ full_name       │         │ expiry_date     │
│ role            │         │ revoked         │
│ created_at      │         │ created_at      │
│ updated_at      │         └─────────────────┘
└────────┬────────┘
         │ 1:N
         │
┌────────▼────────────┐
│ STOCK_TRANSACTIONS  │
├─────────────────────┤
│ id (PK)             │
│ transaction_type    │
│ quantity            │
│ transaction_date    │
│ notes               │
│ product_id (FK)     │───────┐
│ user_id (FK)        │       │
│ created_at          │       │ N:1
│ updated_at          │       │
└─────────────────────┘       │
                              │
         ┌────────────────────▼───────┐
         │       PRODUCTS             │
         ├────────────────────────────┤
         │ id (PK)                    │
         │ name (UQ)                  │
         │ sku (UQ)                   │
         │ description                │
         │ price                      │
         │ quantity_in_stock          │
         │ low_stock_threshold        │
         │ category_id (FK)           │───┐
         │ supplier_id (FK)           │───┼──┐
         │ created_at                 │   │  │
         │ updated_at                 │   │  │
         └────────────────────────────┘   │  │
                                          │  │
     ┌────────────────────────────────────┘  │
     │ N:1                                   │ N:1
     │                                       │
┌────▼──────────┐                  ┌────────▼──────┐
│  CATEGORIES   │                  │   SUPPLIERS   │
├───────────────┤                  ├───────────────┤
│ id (PK)       │                  │ id (PK)       │
│ name (UQ)     │                  │ name (UQ)     │
│ description   │                  │ contact_name  │
│ created_at    │                  │ email (UQ)    │
│ updated_at    │                  │ phone         │
└───────────────┘                  │ address       │
                                   │ created_at    │
                                   │ updated_at    │
                                   └───────────────┘
```

### Database Schema

#### 1. USERS Table

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Constraints:**
- `username`: Unique, 3-20 karakter
- `password`: BCrypt hash, minimum 60 karakter
- `role`: ENUM('ADMIN', 'USER')

#### 2. REFRESH_TOKENS Table

```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Constraints:**
- One-to-One relationship with users
- `token`: Unique, maksimum 500 karakter
- `user_id`: Unique foreign key
- Cascade delete when user deleted

#### 3. CATEGORIES Table

```sql
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Constraints:**
- `name`: Unique, maksimum 100 karakter

#### 4. SUPPLIERS Table

```sql
CREATE TABLE suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    contact_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Constraints:**
- `name`: Unique
- `email`: Unique, valid email format

#### 5. PRODUCTS Table

```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    sku VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 10,
    category_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE RESTRICT,
    INDEX idx_name (name),
    INDEX idx_sku (sku),
    INDEX idx_category (category_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_quantity (quantity_in_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Constraints:**
- `name`: Unique
- `sku`: Unique, Stock Keeping Unit
- `price`: Positive, 2 decimal places
- `quantity_in_stock`: Non-negative
- `low_stock_threshold`: Positive
- Foreign keys with RESTRICT (prevent deletion if referenced)

#### 6. STOCK_TRANSACTIONS Table

```sql
CREATE TABLE stock_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    notes TEXT,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_product (product_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Constraints:**
- `transaction_type`: ENUM('PURCHASE', 'SALE', 'ADJUSTMENT')
- `quantity`: Positive for PURCHASE/ADJUSTMENT, negative for SALE
- Cascade delete when product or user deleted

### Database Indexes

**Performance Optimization:**
- Primary keys: Clustered indexes
- Foreign keys: Non-clustered indexes
- Unique constraints: Unique indexes
- Search columns: Non-clustered indexes

**Index Strategy:**
```sql
-- Fast user lookup
INDEX idx_username ON users(username);

-- Fast token validation
INDEX idx_token ON refresh_tokens(token);
INDEX idx_expiry_date ON refresh_tokens(expiry_date);

-- Fast product search
INDEX idx_name ON products(name);
INDEX idx_sku ON products(sku);
INDEX idx_quantity ON products(quantity_in_stock);

-- Fast transaction queries
INDEX idx_transaction_date ON stock_transactions(transaction_date);
INDEX idx_transaction_type ON stock_transactions(transaction_type);
```

---

## Backend Mimarisi

### Project Structure

```
src/main/java/com/ims/stockmanagement/
├── config/                     # Configuration classes
│   ├── DataInitializer.java   # Admin user initialization
│   ├── ModelMapperConfig.java # DTO mapper configuration
│   ├── OpenAPIConfig.java     # Swagger configuration
│   ├── RestTemplateConfig.java # HTTP client configuration
│   └── TestDataInitializer.java # Test data setup
├── controllers/                # REST Controllers
│   ├── AuthController.java    # Authentication endpoints
│   ├── CategoryController.java
│   ├── DashboardController.java
│   ├── ProductController.java
│   ├── SupplierController.java
│   ├── TransactionController.java
│   └── UserController.java
├── dtos/                       # Data Transfer Objects
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   └── ... (other requests)
│   └── response/
│       ├── AuthResponse.java
│       ├── DashboardStatsResponse.java
│       └── ... (other responses)
├── enums/                      # Enumerations
│   ├── Role.java              # USER, ADMIN
│   └── TransactionType.java  # PURCHASE, SALE, ADJUSTMENT
├── exceptions/                 # Custom Exceptions
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── InvalidCredentialsException.java
│   └── TokenRefreshException.java
├── models/                     # Entity Classes
│   ├── User.java
│   ├── RefreshToken.java
│   ├── Category.java
│   ├── Supplier.java
│   ├── Product.java
│   └── StockTransaction.java
├── repositories/               # JPA Repositories
│   ├── UserRepository.java
│   ├── RefreshTokenRepository.java
│   ├── CategoryRepository.java
│   ├── SupplierRepository.java
│   ├── ProductRepository.java
│   └── TransactionRepository.java
├── security/                   # Security Components
│   ├── JwtAuthenticationFilter.java  # JWT validation filter
│   ├── JwtService.java               # JWT generation/validation
│   ├── SecurityConfig.java           # Security configuration
│   └── UserDetailsServiceImpl.java   # User loading service
├── services/                   # Business Logic
│   ├── AuthService.java
│   ├── RefreshTokenService.java
│   ├── CategoryService.java
│   ├── SupplierService.java
│   ├── ProductService.java
│   ├── TransactionService.java
│   └── UserService.java
├── strategy/                   # Strategy Pattern
│   ├── TransactionStrategy.java (interface)
│   ├── PurchaseStrategy.java
│   ├── SaleStrategy.java
│   └── AdjustmentStrategy.java
└── StockManagementApplication.java # Main application class
```

### Layer Responsibilities

#### 1. Controller Layer

**Responsibilities:**
- HTTP request handling
- Input validation (@Valid)
- DTO conversion
- Response formatting
- HTTP status code management

**Example:**
```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }
}
```

#### 2. Service Layer

**Responsibilities:**
- Business logic implementation
- Transaction management (@Transactional)
- Data validation
- Error handling
- Entity ↔ DTO conversion

**Example:**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    
    public ProductResponse createProduct(ProductRequest request) {
        // Validation
        validateProductRequest(request);
        
        // Business logic
        Product product = modelMapper.map(request, Product.class);
        Product saved = productRepository.save(product);
        
        // Response
        return modelMapper.map(saved, ProductResponse.class);
    }
}
```

#### 3. Repository Layer

**Responsibilities:**
- Database operations
- Custom queries (@Query)
- Pagination and sorting
- Transaction support

**Example:**
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
    Optional<Product> findBySku(String sku);
    List<Product> findByQuantityInStockLessThan(int threshold);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
}
```

### Key Components

#### JWT Service

```java
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; // 15 minutes
    
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 7 days
    
    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
```

#### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

#### Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors,
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

---

## Frontend Mimarisi

### Project Structure

```
frontend/src/
├── components/          # Reusable components
│   ├── Layout.js       # Main layout with navigation
│   └── Layout.css      # Layout styles
├── pages/              # Page components
│   ├── LoginPage.js
│   ├── LoginPage.css
│   ├── RegisterPage.js
│   ├── DashboardPage.js
│   ├── DashboardPage.css
│   ├── ProductPage.js
│   ├── CategoryPage.js
│   ├── SupplierPage.js
│   ├── TransactionPage.js
│   ├── ProfilePage.js
│   └── ProfilePage.css
├── service/            # API and utility services
│   ├── ApiService.js  # Axios configuration and API calls
│   └── Guard.js       # Authentication guard
├── App.js             # Main application component
├── App.css            # Global styles
├── index.js           # Application entry point
└── index.css          # Base styles
```

### Component Architecture

#### 1. Layout Component

**Responsibilities:**
- Application layout structure
- Navigation menu
- User authentication display
- Logout functionality

```javascript
const Layout = ({ children }) => {
    const navigate = useNavigate();
    const [currentUser, setCurrentUser] = useState(null);
    
    useEffect(() => {
        const user = CryptoJS.AES.decrypt(
            localStorage.getItem('user'),
            'secret-key'
        ).toString(CryptoJS.enc.Utf8);
        setCurrentUser(JSON.parse(user));
    }, []);
    
    const handleLogout = () => {
        apiService.logout();
        navigate('/login');
    };
    
    return (
        <div className="layout">
            <nav className="sidebar">{/* Navigation */}</nav>
            <main className="content">{children}</main>
        </div>
    );
};
```

#### 2. Page Components

**DashboardPage:**
- Display statistics
- Low stock alerts
- Recent products
- Quick actions

**ProductPage:**
- Product listing
- Search and filter
- CRUD operations (Admin)
- Stock status display

**CategoryPage:**
- Category management (Admin only)
- Product count per category
- CRUD operations

**SupplierPage:**
- Supplier management (Admin only)
- Contact information
- CRUD operations

**TransactionPage:**
- Transaction history
- Create new transaction (Admin)
- Filter by type/date
- Export functionality

**ProfilePage:**
- User information display
- Profile update
- Password change

### API Service

```javascript
// ApiService.js
import axios from 'axios';
import CryptoJS from 'crypto-js';

const BASE_URL = 'http://localhost:8080/api';
const SECRET_KEY = 'your-secret-key';

// Axios instance with interceptors
const axiosInstance = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Request interceptor - Add JWT token
axiosInstance.interceptors.request.use(
    (config) => {
        const encryptedToken = localStorage.getItem('accessToken');
        if (encryptedToken) {
            const token = CryptoJS.AES.decrypt(
                encryptedToken,
                SECRET_KEY
            ).toString(CryptoJS.enc.Utf8);
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor - Handle token refresh
axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            
            try {
                const newAccessToken = await refreshToken();
                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                return axiosInstance(originalRequest);
            } catch (refreshError) {
                logout();
                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }
        
        return Promise.reject(error);
    }
);

const apiService = {
    // Authentication
    login: async (credentials) => {
        const response = await axiosInstance.post('/auth/login', credentials);
        storeTokens(response.data);
        return response.data;
    },
    
    register: async (userData) => {
        return await axiosInstance.post('/auth/register', userData);
    },
    
    refreshToken: async () => {
        const refreshToken = getDecryptedRefreshToken();
        const response = await axiosInstance.post('/auth/refresh', {
            refreshToken
        });
        storeTokens(response.data);
        return response.data.accessToken;
    },
    
    logout: () => {
        localStorage.clear();
    },
    
    // Products
    getAllProducts: async () => {
        const response = await axiosInstance.get('/products');
        return response.data;
    },
    
    createProduct: async (productData) => {
        return await axiosInstance.post('/products', productData);
    },
    
    updateProduct: async (id, productData) => {
        return await axiosInstance.put(`/products/${id}`, productData);
    },
    
    deleteProduct: async (id) => {
        return await axiosInstance.delete(`/products/${id}`);
    },
    
    // Categories
    getAllCategories: async () => {
        const response = await axiosInstance.get('/categories');
        return response.data;
    },
    
    // Suppliers
    getAllSuppliers: async () => {
        const response = await axiosInstance.get('/suppliers');
        return response.data;
    },
    
    // Transactions
    getAllTransactions: async () => {
        const response = await axiosInstance.get('/transactions');
        return response.data;
    },
    
    createTransaction: async (transactionData) => {
        return await axiosInstance.post('/transactions', transactionData);
    },
    
    // Dashboard
    getDashboardStats: async () => {
        const response = await axiosInstance.get('/dashboard/stats');
        return response.data;
    },
    
    // User
    getCurrentUser: async () => {
        const response = await axiosInstance.get('/users/me');
        return response.data;
    },
    
    updateProfile: async (userData) => {
        return await axiosInstance.put('/users/me', userData);
    }
};

// Helper functions
const storeTokens = (authData) => {
    const encryptedAccess = CryptoJS.AES.encrypt(
        authData.accessToken,
        SECRET_KEY
    ).toString();
    const encryptedRefresh = CryptoJS.AES.encrypt(
        authData.refreshToken,
        SECRET_KEY
    ).toString();
    const encryptedUser = CryptoJS.AES.encrypt(
        JSON.stringify(authData.user),
        SECRET_KEY
    ).toString();
    
    localStorage.setItem('accessToken', encryptedAccess);
    localStorage.setItem('refreshToken', encryptedRefresh);
    localStorage.setItem('user', encryptedUser);
};

export default apiService;
```

### State Management

**Current Approach:** Local component state with useState

**Future Enhancement:** Context API or Redux
```javascript
// Future: AuthContext
const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    
    useEffect(() => {
        // Check authentication status
        checkAuth();
    }, []);
    
    const value = {
        user,
        login,
        logout,
        loading
    };
    
    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};
```

### Routing

```javascript
// App.js
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Guard from './service/Guard';

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                
                <Route path="/" element={<Guard><Layout><DashboardPage /></Layout></Guard>} />
                <Route path="/products" element={<Guard><Layout><ProductPage /></Layout></Guard>} />
                <Route path="/categories" element={<Guard><Layout><CategoryPage /></Layout></Guard>} />
                <Route path="/suppliers" element={<Guard><Layout><SupplierPage /></Layout></Guard>} />
                <Route path="/transactions" element={<Guard><Layout><TransactionPage /></Layout></Guard>} />
                <Route path="/profile" element={<Guard><Layout><ProfilePage /></Layout></Guard>} />
            </Routes>
        </BrowserRouter>
    );
}
```

---

## API Dokümantasyonu

### Authentication Endpoints

#### POST /api/auth/login
Login kullanıcı

**Request:**
```json
{
    "username": "admin",
    "password": "Admin@123!Secure"
}
```

**Response (200 OK):**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "8d7e62a1-9c4b-4f5d-8e3a-1b2c3d4e5f6g",
    "tokenType": "Bearer",
    "user": {
        "id": 1,
        "username": "admin",
        "fullName": "Admin User",
        "role": "ADMIN"
    }
}
```

#### POST /api/auth/register
Yeni kullanıcı kaydı

**Request:**
```json
{
    "username": "newuser",
    "password": "SecurePass@123",
    "fullName": "New User"
}
```

**Response (201 CREATED):**
```json
{
    "id": 2,
    "username": "newuser",
    "fullName": "New User",
    "role": "USER",
    "createdAt": "2024-12-04T10:30:00"
}
```

#### POST /api/auth/refresh
Access token yenileme

**Request:**
```json
{
    "refreshToken": "8d7e62a1-9c4b-4f5d-8e3a-1b2c3d4e5f6g"
}
```

**Response (200 OK):**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "8d7e62a1-9c4b-4f5d-8e3a-1b2c3d4e5f6g",
    "tokenType": "Bearer"
}
```

### Product Endpoints

#### GET /api/products
Tüm ürünleri listele

**Headers:**
```
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
[
    {
        "id": 1,
        "name": "Laptop",
        "sku": "LAP-001",
        "description": "High-performance laptop",
        "price": 1299.99,
        "quantityInStock": 50,
        "lowStockThreshold": 10,
        "category": {
            "id": 1,
            "name": "Electronics"
        },
        "supplier": {
            "id": 1,
            "name": "Tech Suppliers Inc."
        },
        "createdAt": "2024-12-01T10:00:00",
        "updatedAt": "2024-12-04T15:30:00"
    }
]
```

#### POST /api/products
Yeni ürün ekle (Admin only)

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request:**
```json
{
    "name": "Wireless Mouse",
    "sku": "MOU-001",
    "description": "Ergonomic wireless mouse",
    "price": 29.99,
    "quantityInStock": 100,
    "lowStockThreshold": 20,
    "categoryId": 1,
    "supplierId": 2
}
```

**Response (201 CREATED):**
```json
{
    "id": 7,
    "name": "Wireless Mouse",
    "sku": "MOU-001",
    "description": "Ergonomic wireless mouse",
    "price": 29.99,
    "quantityInStock": 100,
    "lowStockThreshold": 20,
    "category": {
        "id": 1,
        "name": "Electronics"
    },
    "supplier": {
        "id": 2,
        "name": "Office Supplies Co."
    },
    "createdAt": "2024-12-04T16:00:00",
    "updatedAt": "2024-12-04T16:00:00"
}
```

#### GET /api/products/{id}
Ürün detayı

**Response (200 OK):**
```json
{
    "id": 1,
    "name": "Laptop",
    "sku": "LAP-001",
    // ... (same as list response)
}
```

#### PUT /api/products/{id}
Ürün güncelle (Admin only)

**Request:**
```json
{
    "name": "Updated Laptop",
    "price": 1399.99,
    "quantityInStock": 45
}
```

**Response (200 OK):**
```json
{
    "id": 1,
    "name": "Updated Laptop",
    "price": 1399.99,
    "quantityInStock": 45,
    // ... (full product data)
}
```

#### DELETE /api/products/{id}
Ürün sil (Admin only)

**Response (204 NO CONTENT)**

### Category Endpoints

#### GET /api/categories
Tüm kategorileri listele

**Response (200 OK):**
```json
[
    {
        "id": 1,
        "name": "Electronics",
        "description": "Electronic devices and accessories",
        "productCount": 15,
        "createdAt": "2024-11-01T10:00:00"
    }
]
```

#### POST /api/categories
Yeni kategori ekle (Admin only)

**Request:**
```json
{
    "name": "Office Supplies",
    "description": "Office equipment and supplies"
}
```

**Response (201 CREATED):**
```json
{
    "id": 4,
    "name": "Office Supplies",
    "description": "Office equipment and supplies",
    "productCount": 0,
    "createdAt": "2024-12-04T16:30:00"
}
```

### Supplier Endpoints

#### GET /api/suppliers
Tüm tedarikçileri listele

**Response (200 OK):**
```json
[
    {
        "id": 1,
        "name": "Tech Suppliers Inc.",
        "contactName": "John Doe",
        "email": "john@techsuppliers.com",
        "phone": "+1-555-0100",
        "address": "123 Tech Street, Silicon Valley, CA",
        "productCount": 10,
        "createdAt": "2024-11-01T10:00:00"
    }
]
```

### Transaction Endpoints

#### GET /api/transactions
Tüm işlemleri listele

**Response (200 OK):**
```json
[
    {
        "id": 1,
        "transactionType": "PURCHASE",
        "quantity": 50,
        "transactionDate": "2024-12-01T10:00:00",
        "notes": "Initial stock",
        "product": {
            "id": 1,
            "name": "Laptop",
            "sku": "LAP-001"
        },
        "user": {
            "id": 1,
            "username": "admin",
            "fullName": "Admin User"
        },
        "createdAt": "2024-12-01T10:00:00"
    }
]
```

#### POST /api/transactions
Yeni işlem kaydet (Admin only)

**Request:**
```json
{
    "transactionType": "SALE",
    "productId": 1,
    "quantity": 5,
    "notes": "Customer order #12345"
}
```

**Response (201 CREATED):**
```json
{
    "id": 15,
    "transactionType": "SALE",
    "quantity": 5,
    "transactionDate": "2024-12-04T16:45:00",
    "notes": "Customer order #12345",
    "product": {
        "id": 1,
        "name": "Laptop",
        "sku": "LAP-001",
        "currentStock": 45
    },
    "user": {
        "id": 1,
        "username": "admin"
    }
}
```

### Dashboard Endpoints

#### GET /api/dashboard/stats
Dashboard istatistikleri

**Response (200 OK):**
```json
{
    "totalProducts": 25,
    "lowStockProducts": 3,
    "totalTransactions": 150,
    "recentProducts": [
        {
            "id": 7,
            "name": "Wireless Mouse",
            "category": "Electronics",
            "quantityInStock": 100,
            "status": "IN_STOCK"
        }
    ],
    "lowStockAlerts": [
        {
            "id": 3,
            "name": "USB Cable",
            "quantityInStock": 5,
            "lowStockThreshold": 20,
            "status": "CRITICAL"
        }
    ]
}
```

### Error Responses

#### 400 Bad Request
```json
{
    "status": 400,
    "message": "Validation failed",
    "errors": [
        "Product name is required",
        "Price must be positive"
    ],
    "timestamp": "2024-12-04T16:50:00"
}
```

#### 401 Unauthorized
```json
{
    "status": 401,
    "message": "Invalid credentials",
    "timestamp": "2024-12-04T16:50:00"
}
```

#### 403 Forbidden
```json
{
    "status": 403,
    "message": "Access denied. Admin role required.",
    "timestamp": "2024-12-04T16:50:00"
}
```

#### 404 Not Found
```json
{
    "status": 404,
    "message": "Product not found with id: 999",
    "timestamp": "2024-12-04T16:50:00"
}
```

---

## Güvenlik Mimarisi

### Authentication Flow

```
1. User Login:
   Client → POST /api/auth/login
   Server → Validate credentials
   Server → Generate JWT access token (15 min)
   Server → Generate refresh token (7 days)
   Server → Save refresh token to database
   Server → Return tokens + user info

2. Authenticated Request:
   Client → GET /api/products
           Header: Authorization: Bearer <access_token>
   Server → JwtAuthenticationFilter intercepts
   Server → Validate token signature
   Server → Check token expiration
   Server → Extract username from token
   Server → Load UserDetails
   Server → Set SecurityContext
   Server → Process request

3. Token Refresh:
   Client → POST /api/auth/refresh
           Body: { refreshToken }
   Server → Validate refresh token
   Server → Check if revoked
   Server → Check expiration
   Server → Generate new access token
   Server → Return new tokens

4. Logout:
   Client → POST /api/auth/logout
   Server → Revoke/delete refresh token
   Client → Clear local storage
```

### JWT Token Structure

**Access Token (15 minutes):**
```json
{
  "sub": "admin",
  "iat": 1701705600,
  "exp": 1701706500,
  "authorities": ["ROLE_ADMIN"]
}
```

**Refresh Token (7 days):**
- UUID string stored in database
- Associated with user_id
- Revoked on logout
- Single-use per user (one-to-one relationship)

### Password Security

**BCrypt Hashing:**
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Cost factor: 12
    }
}
```

**Password Validation:**
- Minimum 8 characters
- At least 1 uppercase letter (A-Z)
- At least 1 lowercase letter (a-z)
- At least 1 digit (0-9)
- At least 1 special character (!@#$%^&*)

**Regex Pattern:**
```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character"
)
private String password;
```

### Authorization

**Role-Based Access Control:**

**ADMIN Role:**
- Full system access
- CRUD operations on all entities
- Create transactions
- View all reports
- User management (future)

**USER Role:**
- Read-only product access
- View dashboard statistics
- View low stock alerts
- Update own profile
- No CRUD operations

**Implementation:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/products")
public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
    return ResponseEntity.ok(productService.createProduct(request));
}

@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
@GetMapping("/products")
public ResponseEntity<List<ProductResponse>> getAllProducts() {
    return ResponseEntity.ok(productService.getAllProducts());
}
```

### Security Headers

```java
http.headers()
    .contentSecurityPolicy("default-src 'self'")
    .and()
    .xssProtection()
    .and()
    .frameOptions().deny()
    .and()
    .httpStrictTransportSecurity()
        .maxAgeInSeconds(31536000)
        .includeSubDomains(true);
```

### CORS Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### SQL Injection Prevention

**JPA Parameterized Queries:**
```java
@Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
List<Product> searchByName(@Param("name") String name);
```

**Input Validation:**
```java
@Valid
@NotBlank(message = "Product name is required")
@Size(min = 3, max = 200)
private String name;
```

### XSS Prevention

**Frontend Sanitization:**
```javascript
const sanitize = (input) => {
    return input.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
};
```

**Backend Output Encoding:**
- Automatic by Spring MVC
- JSON serialization escapes HTML

---

## Deployment

### Local Development

**Backend:**
```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun

# With custom DB password
DB_PASSWORD='YourPassword' ./gradlew bootRun
```

**Frontend:**
```bash
# Install dependencies
cd frontend
npm install

# Development server
npm start

# Production build
npm run build
```

### Docker Deployment

**Dockerfile (Backend):**
```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Dockerfile (Frontend):**
```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**docker-compose.yml:**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: inventory_management_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
  
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/inventory_management_db
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      - mysql
  
  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

### AWS Deployment

Detaylı AWS deployment için `AWS_DEPLOYMENT_GUIDE.md` dökümanına bakınız.

**Özet:**
- **Database:** AWS RDS MySQL 8.0.35
- **Backend:** AWS Elastic Beanstalk (Java 21 Corretto)
- **Frontend:** AWS S3 + CloudFront
- **DNS:** AWS Route 53
- **Monitoring:** CloudWatch

---

## Testing

### Unit Testing

**Backend (JUnit 5 + Mockito):**
```java
@SpringBootTest
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    public void testCreateProduct_Success() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // Act
        ProductResponse response = productService.createProduct(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
```

**Frontend (Jest + React Testing Library):**
```javascript
import { render, screen, fireEvent } from '@testing-library/react';
import LoginPage from './LoginPage';

test('renders login form', () => {
    render(<LoginPage />);
    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
});

test('submits login form', async () => {
    render(<LoginPage />);
    
    fireEvent.change(screen.getByPlaceholderText('Username'), {
        target: { value: 'admin' }
    });
    fireEvent.change(screen.getByPlaceholderText('Password'), {
        target: { value: 'Admin@123!Secure' }
    });
    
    fireEvent.click(screen.getByText('Login'));
    
    // Assert API call
});
```

### Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
```

### API Testing

**Swagger UI:** http://localhost:8080/swagger-ui.html

**Postman Collection:**
- Import OpenAPI spec
- Automated testing
- Environment variables

---

## Performans Optimizasyonu

### Database Optimization

**Indexes:**
```sql
CREATE INDEX idx_product_name ON products(name);
CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_transaction_date ON stock_transactions(transaction_date);
```

**Query Optimization:**
```java
// N+1 problem solution
@Query("SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.supplier")
List<Product> findAllWithDetails();
```

**Connection Pooling:**
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

### Caching

**Spring Cache:**
```java
@Service
public class ProductService {
    @Cacheable("products")
    public List<ProductResponse> getAllProducts() {
        // ...
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        // ...
    }
}
```

### Frontend Optimization

**Code Splitting:**
```javascript
const ProductPage = lazy(() => import('./pages/ProductPage'));
```

**Memoization:**
```javascript
const memoizedValue = useMemo(() => computeExpensiveValue(a, b), [a, b]);
```

**Debouncing:**
```javascript
const debouncedSearch = debounce((term) => {
    searchProducts(term);
}, 300);
```

---

**Teknik Dokümantasyon Sonu**

**Son Güncelleme:** Aralık 2024  
**Versiyon:** 1.0  
**Hazırlayan:** Mehmet Taha Boynikoğlu (212 125 10 34)
