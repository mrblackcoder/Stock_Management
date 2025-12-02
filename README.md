# 📦 Stock Management System (Inventory Management System)

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen)
![React](https://img.shields.io/badge/React-19.2.0-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)

A comprehensive, enterprise-grade web-based inventory management system built with modern technologies. This system allows users to track products, categories, suppliers, and stock transactions with secure authentication and role-based authorization.

## 🎯 Project Overview

**Student Name:** Mehmet Taha Boynikoğlu  
**Student ID:** 212 125 10 34  
**Course:** Web Design and Programming  
**University:** Fatih Sultan Mehmet Vakıf Üniversitesi

### Project Description

This project is a full-stack web application that provides a complete inventory management solution. Users can:
- Register and log in securely with JWT authentication
- Manage products, categories, and suppliers
- Track all stock transactions (purchases, sales, adjustments)
- Monitor inventory levels in real-time with low stock alerts
- Access the system remotely via RESTful API
- Use embedded Thymeleaf pages for authentication

The system implements enterprise-level security with role-based access control (ADMIN/USER) and integrates with external web services for currency conversion.

## 🚀 Quick Start

### Prerequisites
- Java 21+
- MySQL 8.0+
- Node.js 18+
- Git

### Installation

1. **Clone repository**
```bash
git clone <repository-url>
cd Stock_Management
```

2. **Setup database**
```sql
CREATE DATABASE inventory_management_db;
```

3. **Configure application** (if needed)
```bash
# Edit src/main/resources/application.properties
# Update MySQL password if different from Root@12345
```

4. **Run backend**
```bash
./gradlew bootRun
```

5. **Run frontend**
```bash
cd frontend
npm install
npm start
```

6. **Access application**
- 🎨 **Frontend**: http://localhost:3000
- 🔌 **Backend API**: http://localhost:8080
- 📚 **Swagger UI**: http://localhost:8080/swagger-ui.html
- ❤️ **Health Check**: http://localhost:8080/actuator/health
- 📊 **Metrics**: http://localhost:8080/actuator/metrics
- ℹ️ **App Info**: http://localhost:8080/actuator/info
- 🔑 **Default Login**: `admin` / `admin123`

## 📚 Full Documentation

For complete documentation including:
- Detailed architecture
- Database schema
- API endpoints
- Security features
- AWS deployment guide
- Testing instructions

Please see the [Complete Documentation](./DOCUMENTATION.md)

## 🛠️ Technology Stack

**Backend:** Java 21, Spring Boot 3.5.7, Spring Security, MySQL, JWT  
**Frontend:** React 19, Axios, Bootstrap 5, Thymeleaf  
**Tools:** Gradle, Git, IntelliJ IDEA

## 📡 Key API Endpoints

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

## 🗄️ Database Schema

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

## ✨ Features

- ✅ JWT Authentication & Authorization
- ✅ Role-based Access Control (ADMIN/USER)
- ✅ Complete CRUD Operations
- ✅ Stock Transaction Management
- ✅ Low Stock Alerts
- ✅ External API Integration
- ✅ Responsive UI
- ✅ RESTful API
- ✅ Secure Password Hashing (BCrypt)
- ✅ Client-side Token Encryption

## 📞 Contact

**Mehmet Taha Boynikoğlu**  
Student ID: 212 125 10 34  
Fatih Sultan Mehmet Vakıf Üniversitesi

---

**Last Updated:** December 2024  
**Version:** 1.0.0  
**Status:** ✅ Production Ready
