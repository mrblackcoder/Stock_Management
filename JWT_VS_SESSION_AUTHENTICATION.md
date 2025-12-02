# JWT vs Session-Based Authentication

## Overview

This document explains the authentication approach used in this Stock Management System and how it relates to the security concepts covered in the Web Design and Programming course.

## Table of Contents

- [Authentication Approaches](#authentication-approaches)
- [JWT Authentication (Our Implementation)](#jwt-authentication-our-implementation)
- [Session-Based Authentication (Traditional Approach)](#session-based-authentication-traditional-approach)
- [Course Requirements Mapping](#course-requirements-mapping)
- [Implementation Details](#implementation-details)

---

## Authentication Approaches

There are two primary approaches to handling authentication in web applications:

### 1. Session-Based Authentication (Traditional)
- Server stores session data in memory or database
- Browser receives session ID as a cookie
- Server looks up session data for each request
- Requires server-side session management

### 2. JWT (JSON Web Token) Authentication (Modern)
- Server creates a signed token containing user information
- Browser stores token (localStorage or cookie)
- Token is sent with each request
- Server validates token signature without database lookup
- **Stateless** - no server-side session storage needed

---

## JWT Authentication (Our Implementation)

### Why We Chose JWT

This project uses **JWT (JSON Web Token)** authentication for the following reasons:

#### 1. **Stateless Architecture**
```
Traditional Session:
Browser → Send Session ID → Server checks database → Response

JWT:
Browser → Send JWT → Server validates signature → Response
```

- **No server-side storage**: The server doesn't need to maintain session data
- **Scalability**: Easier to scale horizontally (multiple servers)
- **Microservices-ready**: Tokens can be validated by any service

#### 2. **Decoupled Frontend-Backend**
- React frontend runs on `localhost:3000`
- Spring Boot backend runs on `localhost:8080`
- JWT tokens work seamlessly across different domains
- No CORS issues with session cookies

#### 3. **Mobile & API-First Design**
- Same JWT can be used by:
  - Web application (React)
  - Mobile apps (iOS/Android)
  - Third-party API consumers
  - Desktop applications

#### 4. **Token-Based Authorization**
- JWT payload contains user role and permissions
- Server can make authorization decisions without database lookup
- Faster request processing

### JWT Structure in Our Application

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "admin",
    "role": "ROLE_ADMIN",
    "iat": 1701234567,
    "exp": 1701320967
  },
  "signature": "HMACSHA256(...)"
}
```

### JWT Flow in Our Application

```
┌─────────┐                 ┌──────────────┐
│ Browser │                 │ Spring Boot  │
│ (React) │                 │   Backend    │
└────┬────┘                 └──────┬───────┘
     │                              │
     │ 1. POST /api/auth/login     │
     │ { username, password }       │
     ├─────────────────────────────>│
     │                              │
     │                              │ 2. Validate credentials
     │                              │    Generate JWT token
     │                              │
     │ 3. Return JWT token         │
     │<─────────────────────────────┤
     │                              │
     │ Store token in localStorage  │
     │                              │
     │ 4. GET /api/products         │
     │ Header: Authorization:       │
     │         Bearer <JWT>         │
     ├─────────────────────────────>│
     │                              │
     │                              │ 5. Validate JWT signature
     │                              │    Extract user info
     │                              │    Check authorization
     │                              │
     │ 6. Return products data     │
     │<─────────────────────────────┤
     │                              │
```

---

## Session-Based Authentication (Traditional Approach)

### How Session-Based Authentication Works

```
┌─────────┐                 ┌──────────────┐        ┌──────────┐
│ Browser │                 │ Spring Boot  │        │ Session  │
│         │                 │   Backend    │        │  Store   │
└────┬────┘                 └──────┬───────┘        └────┬─────┘
     │                              │                     │
     │ 1. POST /login              │                     │
     │ { username, password }       │                     │
     ├─────────────────────────────>│                     │
     │                              │                     │
     │                              │ 2. Validate        │
     │                              │    Create session  │
     │                              ├────────────────────>│
     │                              │                     │
     │ 3. Set-Cookie:              │                     │
     │    JSESSIONID=ABC123        │                     │
     │<─────────────────────────────┤                     │
     │                              │                     │
     │ 4. GET /products             │                     │
     │ Cookie: JSESSIONID=ABC123    │                     │
     ├─────────────────────────────>│                     │
     │                              │                     │
     │                              │ 5. Lookup session  │
     │                              ├────────────────────>│
     │                              │                     │
     │                              │ 6. Session data    │
     │                              │<────────────────────┤
     │                              │                     │
     │ 7. Return products          │                     │
     │<─────────────────────────────┤                     │
     │                              │                     │
```

### Features of Session-Based Authentication

#### 1. **Session Management** (from Course Lecture)
```java
// Session-based configuration (NOT used in our JWT app)
.sessionManagement(session -> session
    .sessionFixation().migrateSession()     // New session ID on login
    .maximumSessions(1)                     // Max 1 device per user
    .expiredUrl("/login?expired=true")
)

// Session timeout
server.servlet.session.timeout=30m
```

**Why this doesn't apply to JWT:**
- JWT is **stateless** - there are no server-side sessions
- Token expiration is handled by the `exp` claim in the JWT payload
- Multiple devices can use different tokens simultaneously

#### 2. **Remember-Me Functionality** (from Course Lecture)
```java
// Session-based Remember-Me (NOT used in our JWT app)
.rememberMe(remember -> remember
    .key("unique-secret-key")
    .tokenValiditySeconds(7 * 24 * 60 * 60)  // 7 days
    .userDetailsService(userDetailsService)
)
```

**JWT Equivalent:**
```java
// In our JwtService.java
public String generateToken(User user) {
    return Jwts.builder()
        .setSubject(user.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))  // 24 hours
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

**How JWT handles "Remember Me":**
- Instead of a server-side remember-me cookie, JWT tokens have built-in expiration
- Frontend can implement "Remember Me" by:
  - Storing token in `localStorage` (persistent across browser sessions)
  - Not storing token (session-only, cleared when browser closes)
- Token refresh mechanism can extend login duration without re-authentication

#### 3. **Session Fixation Attack Prevention**
```java
// Session-based protection (NOT needed in JWT)
.sessionManagement(session -> session
    .sessionFixation().migrateSession()  // Change session ID after login
)
```

**Why JWT doesn't need this:**
- Session fixation attacks exploit predictable session IDs
- JWT tokens are cryptographically signed and include timestamp
- Each login generates a completely new JWT token
- Old tokens are invalidated when they expire

---

## Course Requirements Mapping

### ✅ Security Topics from Lecture 7 (Security)

| Course Topic | Session-Based | JWT (Our Implementation) | Status |
|--------------|---------------|--------------------------|--------|
| **HTTP/HTTPS & SSL/TLS** | ✓ | ✓ | ✅ Implemented |
| **BCrypt Password Hashing** | ✓ | ✓ | ✅ Implemented |
| **SQL Injection Prevention** | ✓ | ✓ | ✅ Implemented (JPA) |
| **XSS Protection** | ✓ | ✓ | ✅ Implemented (Headers) |
| **CSRF Protection** | ✓ (Token) | ✓ (Disabled for JWT) | ✅ Implemented |
| **Session Hijacking** | ✓ (Cookie Security) | ✓ (Token Security) | ✅ Implemented |
| **Brute Force Protection** | ✓ | ✓ | ✅ Implemented (LoginAttemptService) |
| **Security Headers** | ✓ | ✓ | ✅ Implemented (All headers) |
| **Role-Based Access Control** | ✓ | ✓ | ✅ Implemented |
| **Method-Level Security** | ✓ | ✓ | ✅ Implemented (@PreAuthorize) |
| **Role Hierarchy** | ✓ | ✓ | ✅ Implemented |
| **Session Management** | ✓ | N/A (Stateless) | ℹ️ Not applicable to JWT |
| **Remember-Me** | ✓ (Cookie) | ✓ (Token expiration) | ✅ Different implementation |

### Why CSRF is Disabled in JWT Applications

```java
// In our SecurityConfig.java
.csrf(AbstractHttpConfigurer::disable)
```

**Explanation:**
- **CSRF attacks** exploit browser's automatic cookie sending
- **Session-based auth**: Browser automatically sends session cookie with every request
- **JWT auth**: Token is manually added to `Authorization` header by JavaScript
- Attacker's site **cannot access** tokens stored in `localStorage`
- CSRF protection **not needed** for JWT stored in localStorage

**Important Note:** If JWT were stored in cookies, CSRF protection would be required!

---

## Implementation Details

### Our Security Configuration (SecurityConfig.java)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // ← Enables @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // Disabled for JWT
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless session management for JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Security headers (same as session-based)
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.headerValue("1; mode=block"))
                .contentSecurityPolicy(csp -> csp.policyDirectives("..."))
            )

            // JWT authentication filter
            .addFilterBefore(jwtAuthenticationFilter,
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Role Hierarchy (works with both JWT and Session)
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return hierarchy;
    }
}
```

### Method-Level Security Examples

```java
@Service
@RequiredArgsConstructor
public class ProductService {

    // Any authenticated user can create products
    @PreAuthorize("isAuthenticated()")
    public Response createProduct(ProductDTO productDTO) { ... }

    // Only ADMIN can delete products
    @PreAuthorize("hasRole('ADMIN')")
    public Response deleteProduct(Long id) { ... }

    // Complex: ADMIN or product owner can update
    @PreAuthorize("hasRole('ADMIN') or #productId == @productService.getOwnerId(#id)")
    public Response updateProduct(Long id, ProductDTO dto) { ... }
}
```

### JWT Token Security

```java
@Service
@RequiredArgsConstructor
public class JwtService {

    // Token generation with expiration
    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("role", user.getRole())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))  // 24h
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    // Token validation
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```

---

## Comparison Table

| Feature | Session-Based | JWT (Our App) |
|---------|---------------|---------------|
| **Storage Location** | Server (database/memory) | Client (localStorage) |
| **Scalability** | Limited (sticky sessions) | High (stateless) |
| **Server Memory** | Required | Not required |
| **Logout** | Delete server session | Token expiration/blacklist |
| **Token Size** | Small cookie (session ID) | Larger (contains user data) |
| **Cross-Domain** | Difficult | Easy |
| **Mobile Apps** | Difficult | Easy |
| **Security** | Secure if properly configured | Secure if properly implemented |
| **Implementation Complexity** | Lower | Moderate |
| **Course Examples** | ✓ | ✓ (Modern equivalent) |

---

## Conclusion

### Why Our JWT Implementation Satisfies Course Requirements

1. **Security Fundamentals** (All implemented ✅)
   - Authentication: ✅ JWT-based login system
   - Authorization: ✅ Role-based access control
   - Password Security: ✅ BCrypt hashing
   - Attack Prevention: ✅ XSS, SQL Injection, Brute Force, Clickjacking

2. **Advanced Security** (All implemented ✅)
   - Method-Level Security: ✅ `@PreAuthorize` annotations
   - Role Hierarchy: ✅ ADMIN > USER
   - Security Headers: ✅ All recommended headers
   - Secure Communication: ✅ HTTPS/SSL support

3. **Session vs JWT** (Equivalents provided ℹ️)
   - Session Management: ℹ️ JWT is stateless (by design)
   - Remember-Me: ℹ️ JWT token expiration (equivalent)
   - Session Fixation: ℹ️ Not applicable (tokens regenerated on login)

### Modern Best Practices

Our JWT implementation follows industry best practices:
- ✅ Stateless architecture for scalability
- ✅ Decoupled frontend-backend
- ✅ API-first design for multiple clients
- ✅ Secure token generation and validation
- ✅ Comprehensive authorization controls
- ✅ All security headers implemented

### Learning Outcome

This project demonstrates understanding of:
1. Traditional session-based authentication concepts (from lectures)
2. Modern JWT-based authentication implementation
3. Security best practices applicable to both approaches
4. When to use each approach and why

---

## References

### Course Materials
- Lecture 7: Security (Session-based examples)
- Lecture 6: Decoupled Web Architecture (JWT use case)

### Implementation Files
- `SecurityConfig.java` - Main security configuration
- `JwtService.java` - JWT token generation and validation
- `JwtAuthenticationFilter.java` - Request authentication
- `LoginAttemptService.java` - Brute force protection
- Service classes with `@PreAuthorize` - Method-level security

### External Resources
- [JWT.io](https://jwt.io/) - JWT introduction and debugger
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

---

**Author:** Mehmet Taha Boynikoğlu (212 125 10 34)
**Course:** Web Design and Programming
**Date:** December 2025
