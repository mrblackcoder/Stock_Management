# Stock Management System (Inventory Management System)

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen)
![React](https://img.shields.io/badge/React-19.2.0-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)

A full-stack web-based inventory management application built with Java 21, Spring Boot, Spring Security,
Spring Data JPA, MySQL and React. It lets users manage products, categories, suppliers and stock
transactions behind JWT authentication and role-based authorization.

## Project Overview

The project demonstrates REST API design, relational data modelling, JWT-based authentication,
role-based authorization (ADMIN/USER), Docker-based containerization and CI workflow configuration.

Users can:
- Register and log in with JWT authentication
- Manage products, categories, and suppliers
- Track stock transactions (purchases, sales, adjustments)
- Monitor inventory levels with low-stock alerts
- Access the system via a RESTful API
- Convert product prices using an external currency-exchange service

## Quick Start

### Prerequisites
- JDK 21
- Docker and Docker Compose
- Node.js 18+ and npm, when running the frontend outside Docker

> The project targets Java 21 through Gradle toolchains. Use JDK 21 for consistent local and CI builds.
> The current Gradle 8.14.3 wrapper is not officially supported for running on JDK 25 or JDK 26.

### Run with Docker (recommended)
```bash
git clone https://github.com/mrblackcoder/Stock_Management.git
cd Stock_Management
cp .env.docker.example .env   # then edit .env and set your own secrets
docker compose up --build
```

### Run locally (backend + frontend)

1. **Create the database**
```sql
CREATE DATABASE inventory_management_db;
```

2. **Configure the backend** — set your DB credentials and a JWT secret via environment variables
   (`JWT_SECRET`, `ADMIN_PASSWORD`, DB settings). See `src/main/resources/application.properties` for the
   keys and `.env.example` for the expected variables.

3. **Run the backend** (JDK 21)
```bash
./gradlew bootRun
```

4. **Run the frontend**
```bash
cd frontend
npm install
npm start
```

5. **Access the application**
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

> The initial admin account is created from the `ADMIN_PASSWORD` environment variable on first startup.
> No default credentials are shipped in this repository — set your own.

## Documentation

- [Architecture](./docs/ARCHITECTURE.md)
- [Database Schema](./docs/DATABASE_SCHEMA.md)
- [Deployment Guide](./DEPLOYMENT_GUIDE.md)

## Technology Stack

**Backend:** Java 21, Spring Boot 3.5.7, Spring Security, Spring Data JPA, MySQL, JWT
**Frontend:** React 19, Axios, Bootstrap 5
**Tooling:** Gradle, Docker, GitHub Actions, Swagger/OpenAPI

## Key API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |
| GET | `/api/products` | Get all products |
| POST | `/api/products` | Create new product |
| GET | `/api/products/low-stock` | Get low stock alerts |
| GET | `/api/categories` | Get all categories |
| GET | `/api/suppliers` | Get all suppliers |
| POST | `/api/transactions` | Record stock transaction |

## Database Schema

**Tables:**
- `users` - System users with roles
- `products` - Inventory items
- `categories` - Product categories
- `suppliers` - Product suppliers
- `stock_transactions` - All inventory movements

**Relationships:**
- Product → Category (Many-to-One)
- Product → Supplier (Many-to-One)
- Product → Transactions (One-to-Many)
- User → Transactions (One-to-Many)

## Features

- JWT authentication with refresh-token rotation
- Role-based access control (ADMIN/USER)
- CRUD operations for products, categories and suppliers
- Stock transaction management (purchase / sale / adjustment)
- Low-stock alerts
- External currency-conversion API integration
- BCrypt password hashing and brute-force login lockout
- Pagination and sorting on list endpoints
- Responsive React UI
- RESTful API documented with Swagger/OpenAPI

## Background

The project was initially developed as a university coursework project and was later extended as a
Java/Spring Boot portfolio application.

## Contact

**Mehmet Taha Boynikoğlu**
GitHub: https://github.com/mrblackcoder/Stock_Management

---

**Version:** 1.0.0
**License:** MIT
