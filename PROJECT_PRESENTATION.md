# Inventory Management System
## Project Presentation Document

---

## Student Information

**Student Name:** Mehmet Taha Boynikoğlu  
**Student ID:** 212 125 10 34  
**Course:** Web Design and Programming  
**Instructor:** [Instructor Name]  
**University:** Fatih Sultan Mehmet Vakıf Üniversitesi  
**Submission Date:** December 2024

---

## Executive Summary

This project presents a comprehensive, enterprise-grade inventory management system developed as a full-stack web application. The system demonstrates modern software engineering principles, secure authentication mechanisms, and professional development practices suitable for real-world business applications.

The application provides complete inventory control capabilities including product management, supplier tracking, category organization, and detailed transaction logging. Built with industry-standard technologies, the system features secure JWT-based authentication, role-based access control, and external API integration for currency conversion services.

---

## Project Overview

### Purpose

The Inventory Management System (IMS) serves as a complete solution for businesses to track and manage their inventory digitally. The system addresses common inventory management challenges:

- Real-time stock level monitoring
- Automated low-stock alerts
- Complete transaction history tracking
- Secure multi-user access with role-based permissions
- Supplier and category management
- Professional reporting and dashboards

### Scope

The project encompasses:
- Full-stack web application development
- RESTful API design and implementation
- Secure authentication and authorization
- Database design and optimization
- Responsive user interface development
- External API integration
- Production-ready deployment configuration

---

## Technical Architecture

### Technology Stack

**Backend Technologies:**
- **Programming Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.5.7
- **Security:** Spring Security 6 with JWT Authentication
- **Database:** MySQL 8.0
- **Build Tool:** Gradle 8.14.3
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Monitoring:** Spring Boot Actuator

**Frontend Technologies:**
- **Library:** React 19.2.0
- **UI Framework:** Bootstrap 5
- **HTTP Client:** Axios
- **Security:** CryptoJS for client-side encryption
- **Template Engine:** Thymeleaf (for authentication pages)

**Development Tools:**
- **IDE:** IntelliJ IDEA
- **Version Control:** Git & GitHub
- **API Testing:** Postman, Swagger UI

### Architecture Pattern

The application follows a modern **three-tier architecture**:

1. **Presentation Layer** (Frontend)
   - React-based Single Page Application (SPA)
   - Responsive UI components
   - Client-side routing and state management

2. **Business Logic Layer** (Backend)
   - RESTful API services
   - Business logic processing
   - Security and authentication
   - Data validation

3. **Data Layer**
   - MySQL relational database
   - Spring Data JPA repositories
   - Entity relationship management

### Design Patterns Implemented

1. **MVC Pattern:** Clear separation between Model, View, and Controller
2. **Repository Pattern:** Data access abstraction through Spring Data JPA
3. **DTO Pattern:** Data Transfer Objects for API communication
4. **Strategy Pattern:** Flexible admin operation handling (bulk operations, reports)
5. **Dependency Injection:** Spring Framework's IoC container
6. **Builder Pattern:** Entity construction and API response building

---

## Database Design

### Entity-Relationship Model

**Core Entities:**

1. **User**
   - User authentication and profile information
   - Role-based access control (ADMIN/USER)
   - Password security with BCrypt hashing

2. **Product**
   - Inventory item details
   - Stock quantity tracking
   - Price information
   - Low stock threshold alerts

3. **Category**
   - Product classification
   - Hierarchical organization support

4. **Supplier**
   - Supplier contact information
   - Supplier-product relationships

5. **StockTransaction**
   - Complete audit trail of inventory movements
   - Transaction types: PURCHASE, SALE, ADJUSTMENT
   - User tracking for accountability

6. **RefreshToken**
   - Secure token management
   - Automatic token rotation
   - Revocation support

### Relationships

- Product → Category (Many-to-One)
- Product → Supplier (Many-to-One)
- Product → StockTransaction (One-to-Many)
- User → StockTransaction (One-to-Many)
- User → RefreshToken (One-to-One)

### Database Schema Highlights

- Foreign key constraints for referential integrity
- Cascade operations for related entity management
- Indexed columns for query optimization
- Temporal tracking (creation/update timestamps)

---

## Core Features & Functionality

### 1. Authentication & Security

**JWT-Based Authentication:**
- Secure token-based authentication
- Access token (15-minute expiration)
- Refresh token (7-day expiration)
- Automatic token refresh mechanism
- Token revocation on logout

**Password Security:**
- BCrypt hashing algorithm
- Minimum 8 characters requirement
- Complexity validation (uppercase, lowercase, digits, special characters)
- Client-side encryption before transmission

**Role-Based Access Control (RBAC):**
- ADMIN role: Full system access
- USER role: Limited operations
- Method-level security annotations
- Role hierarchy implementation

### 2. Product Management

- Complete CRUD operations
- Product search functionality
- Filter by category
- Low stock alerts (configurable threshold)
- Price management
- Stock quantity tracking
- Product image support
- Bulk operations (Admin only)

### 3. Category Management

- Hierarchical category structure
- Category-based product filtering
- CRUD operations
- Product count per category

### 4. Supplier Management

- Supplier information management
- Contact details tracking
- Supplier-product associations
- Supplier performance tracking

### 5. Stock Transaction Management

- Complete transaction history
- Transaction types:
  - PURCHASE: Incoming stock
  - SALE: Outgoing stock
  - ADJUSTMENT: Inventory corrections
- Real-time stock updates
- User accountability tracking
- Transaction reporting

### 6. Dashboard & Reporting

- Real-time inventory statistics
- Low stock alerts
- Recent transactions view
- Product summary
- Transaction history
- Visual indicators for critical stock levels

### 7. External API Integration

- Currency conversion service integration
- Real-time exchange rate display
- ExchangeRate-API integration
- Error handling and fallback mechanisms

---

## API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | User login | Public |
| POST | `/api/auth/logout` | User logout | Authenticated |
| POST | `/api/auth/refresh` | Refresh access token | Authenticated |

### Product Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/products` | Get all products | Authenticated |
| GET | `/api/products/{id}` | Get product by ID | Authenticated |
| POST | `/api/products` | Create product | Admin |
| PUT | `/api/products/{id}` | Update product | Admin |
| DELETE | `/api/products/{id}` | Delete product | Admin |
| GET | `/api/products/search` | Search products | Authenticated |
| GET | `/api/products/category/{id}` | Products by category | Authenticated |
| GET | `/api/products/low-stock` | Low stock alerts | Authenticated |

### Category Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/categories` | Get all categories | Authenticated |
| GET | `/api/categories/{id}` | Get category by ID | Authenticated |
| POST | `/api/categories` | Create category | Admin |
| PUT | `/api/categories/{id}` | Update category | Admin |
| DELETE | `/api/categories/{id}` | Delete category | Admin |

### Supplier Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/suppliers` | Get all suppliers | Authenticated |
| GET | `/api/suppliers/{id}` | Get supplier by ID | Authenticated |
| POST | `/api/suppliers` | Create supplier | Admin |
| PUT | `/api/suppliers/{id}` | Update supplier | Admin |
| DELETE | `/api/suppliers/{id}` | Delete supplier | Admin |

### Transaction Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/transactions` | Get all transactions | Authenticated |
| GET | `/api/transactions/{id}` | Get transaction by ID | Authenticated |
| POST | `/api/transactions` | Create transaction | Admin |
| GET | `/api/transactions/product/{id}` | Transactions by product | Authenticated |

### Admin Strategy Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| DELETE | `/api/admin/products/bulk-delete` | Bulk delete products | Admin |
| POST | `/api/admin/products/update-prices` | Update product prices | Admin |
| GET | `/api/admin/reports` | Generate reports | Admin |

---

## Security Implementation

### Authentication Flow

1. User submits credentials (username, encrypted password)
2. Backend validates credentials
3. JWT tokens generated (access + refresh)
4. Tokens encrypted and stored client-side
5. Subsequent requests include access token
6. Token validation on each request
7. Automatic refresh when access token expires

### Security Features

- **Password Encryption:** BCrypt with salt
- **Token Security:** HMAC SHA-512 signing
- **CORS Configuration:** Controlled cross-origin access
- **SQL Injection Prevention:** Parameterized queries via JPA
- **XSS Protection:** Input validation and sanitization
- **CSRF Protection:** Token-based request validation
- **Session Management:** Stateless JWT approach
- **Role Enforcement:** Method-level security

### Security Best Practices Implemented

- No sensitive data in JWT payload
- Secure password requirements
- Token expiration and rotation
- Automatic token revocation
- Audit trail through transaction logging
- Input validation at multiple layers

---

## User Interface Design

### Design Principles

- **Responsive Design:** Mobile-first approach with Bootstrap
- **User Experience:** Intuitive navigation and clear workflows
- **Accessibility:** Semantic HTML and ARIA labels
- **Performance:** Lazy loading and optimized rendering
- **Consistency:** Uniform styling and component patterns

### Key UI Components

1. **Login/Register Pages**
   - Clean, professional authentication interface
   - Real-time validation feedback
   - Secure password input

2. **Dashboard**
   - Quick statistics overview
   - Low stock alerts
   - Recent activity feed
   - Visual indicators

3. **Product Management**
   - Searchable product grid
   - Detailed product forms
   - Inline editing capabilities
   - Currency conversion display

4. **Transaction Interface**
   - Transaction type selection
   - Product lookup
   - Quantity input with validation
   - Transaction history table

5. **Category & Supplier Management**
   - Tabular data display
   - Sorting and filtering
   - Modal-based forms
   - Relationship visualization

### Responsive Behavior

- Adapts to desktop, tablet, and mobile screens
- Touch-friendly interface elements
- Collapsible navigation for mobile
- Optimized content layout per screen size

---

## Testing & Quality Assurance

### Testing Strategy

1. **Unit Testing**
   - Service layer testing with JUnit 5
   - Repository testing with DataJpaTest
   - Mock objects using Mockito

2. **Integration Testing**
   - End-to-end API testing
   - Database integration validation
   - Security configuration testing

3. **Manual Testing**
   - User interface testing
   - Cross-browser compatibility
   - Responsive design validation
   - User workflow verification

### Quality Metrics

- Code coverage analysis
- Static code analysis
- Security vulnerability scanning
- Performance profiling

---

## Deployment & Configuration

### Application Configuration

**Database Configuration:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management_db
spring.datasource.username=root
spring.datasource.password=[CONFIGURED]
spring.jpa.hibernate.ddl-auto=update
```

**Security Configuration:**
```properties
jwt.secret=[SECURE_SECRET_KEY]
jwt.access-token.expiration=900000    # 15 minutes
jwt.refresh-token.expiration=604800000 # 7 days
```

**Server Configuration:**
```properties
server.port=8080
spring.application.name=StockManagement
```

### Monitoring & Actuator

Enabled Spring Boot Actuator endpoints:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Performance metrics
- `/actuator/env` - Environment properties (secured)

### Deployment Options

1. **Local Development:** Embedded Tomcat server
2. **Production:** Standalone JAR deployment
3. **Cloud Ready:** Containerization support (Docker)
4. **Database:** MySQL on local or cloud (AWS RDS, etc.)

---

## Project Management & Development Process

### Version Control

- **Repository:** GitHub (mrblackcoder/Stock_Management)
- **Branch Strategy:** Main branch with feature commits
- **Commit Convention:** Descriptive commit messages
- **Documentation:** Code comments and README files

### Development Workflow

1. Requirements analysis
2. Database schema design
3. Backend API development
4. Frontend interface development
5. Integration and testing
6. Security hardening
7. Documentation
8. Deployment preparation

### Project Timeline

- **Week 1-2:** Project planning and architecture design
- **Week 3-4:** Backend development (entities, repositories, services)
- **Week 5-6:** Security implementation (JWT, Spring Security)
- **Week 7-8:** Frontend development (React components, UI)
- **Week 9-10:** Integration, testing, and refinement
- **Week 11-12:** Documentation and deployment preparation

---

## Challenges & Solutions

### Challenge 1: JWT Token Management
**Problem:** Secure token storage and automatic refresh
**Solution:** Client-side encryption with CryptoJS and automatic token refresh interceptor

### Challenge 2: Refresh Token Unique Constraint
**Problem:** Duplicate key error on token regeneration
**Solution:** Changed token revocation strategy from UPDATE to DELETE

### Challenge 3: External API Integration
**Problem:** Currency conversion service integration with error handling
**Solution:** Implemented RestTemplate with fallback mechanisms

### Challenge 4: Role-Based Access Control
**Problem:** Granular permission management across endpoints
**Solution:** Method-level security with @PreAuthorize annotations

### Challenge 5: Transaction Consistency
**Problem:** Maintaining stock accuracy during concurrent transactions
**Solution:** Database transactions with proper isolation levels

---

## Future Enhancements

1. **Advanced Reporting:**
   - PDF/Excel report generation
   - Visual charts and graphs
   - Sales analytics

2. **Inventory Forecasting:**
   - Machine learning-based stock predictions
   - Seasonal trend analysis
   - Automated reorder suggestions

3. **Multi-Warehouse Support:**
   - Multiple location tracking
   - Transfer management
   - Location-based stock allocation

4. **Mobile Application:**
   - Native iOS/Android apps
   - Barcode scanning
   - Offline mode support

5. **Email Notifications:**
   - Low stock alerts
   - Transaction confirmations
   - System notifications

6. **Advanced Search:**
   - Full-text search
   - Filter combinations
   - Saved search preferences

---

## Conclusion

This Inventory Management System demonstrates comprehensive understanding of modern web application development, including:

- Full-stack development capabilities
- Secure authentication and authorization
- RESTful API design principles
- Database design and optimization
- User interface development
- Software engineering best practices

The project successfully implements enterprise-grade features suitable for real-world deployment, showcasing technical proficiency in Java, Spring Boot, React, and related technologies. The system is production-ready, secure, and scalable, meeting all requirements for a professional inventory management solution.

---

## References & Resources

### Technologies & Frameworks
- Spring Framework Documentation: https://spring.io/projects/spring-boot
- React Documentation: https://react.dev
- MySQL Documentation: https://dev.mysql.com/doc/
- JWT Introduction: https://jwt.io/introduction

### External APIs
- ExchangeRate-API: https://www.exchangerate-api.com

### Development Tools
- IntelliJ IDEA: https://www.jetbrains.com/idea/
- Postman: https://www.postman.com
- Git: https://git-scm.com

---

## Appendices

### Appendix A: Installation Guide
See `QUICK_START.md` for detailed installation instructions.

### Appendix B: API Documentation
Interactive API documentation available at: http://localhost:8080/swagger-ui.html

### Appendix C: Database Schema
Complete database schema with relationships documented in code comments.

### Appendix D: Source Code
Full source code available on GitHub: https://github.com/mrblackcoder/Stock_Management

---

**Document Version:** 1.0  
**Last Updated:** December 2024  
**Status:** Final Submission

---

## Declaration

I declare that this project is my original work and has been completed in accordance with academic integrity policies. All external sources and references have been properly cited.

**Student Signature:** ___________________  
**Date:** ___________________

---

**End of Document**
