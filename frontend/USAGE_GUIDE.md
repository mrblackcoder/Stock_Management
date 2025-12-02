# 📖 Stock Management System - Usage Guide

## Quick Links

- 🌐 **Swagger UI**: http://localhost:8080/swagger-ui.html
- 📊 **API Docs**: http://localhost:8080/api-docs
- ❤️ **Health Check**: http://localhost:8080/actuator/health
- 📈 **Metrics**: http://localhost:8080/actuator/metrics
- ℹ️ **App Info**: http://localhost:8080/actuator/info
- 🎨 **Frontend**: http://localhost:3000

---

## 🚀 Getting Started

### 1. Start the Application

**Option A: Using Gradle (Development)**
```bash
# Terminal 1 - Backend
./gradlew bootRun

# Terminal 2 - Frontend
cd frontend
npm install
npm start
```

**Option B: Using Docker**
```bash
# Start all services (MySQL + Backend)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### 2. Access the Application

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | admin / admin123 |
| Swagger UI | http://localhost:8080/swagger-ui.html | - |
| Backend API | http://localhost:8080/api | JWT Token |
| Actuator | http://localhost:8080/actuator | - |

---

## 📡 Using the API

### Method 1: Swagger UI (Recommended for Testing)

1. **Open Swagger UI**: http://localhost:8080/swagger-ui.html

2. **Register/Login**:
   - Try the `/api/auth/register` endpoint to create an account
   - Use `/api/auth/login` to get your JWT token
   - Copy the `token` from the response

3. **Authorize**:
   - Click the 🔓 "Authorize" button (top right)
   - Enter: `Bearer YOUR_TOKEN_HERE`
   - Click "Authorize" and "Close"

4. **Test Endpoints**:
   - All endpoints are now accessible
   - Click "Try it out" on any endpoint
   - Fill in parameters and click "Execute"

### Method 2: Using cURL

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'

# 2. Login and save token
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }' | jq -r '.token')

echo "Token: $TOKEN"

# 3. Get all products
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"

# 4. Create a product
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Dell XPS 15",
    "sku": "LAP-001",
    "price": 1500.00,
    "stockQuantity": 50,
    "reorderLevel": 10,
    "categoryId": 1,
    "supplierId": 1
  }'
```

### Method 3: Using Postman

1. **Import Collection**:
   - Open Postman
   - Click "Import"
   - Paste Swagger URL: `http://localhost:8080/v3/api-docs`
   - Postman auto-generates collection

2. **Set Environment**:
   - Create new environment "Stock Management Dev"
   - Add variable: `baseUrl` = `http://localhost:8080`
   - Add variable: `token` (will be filled after login)

3. **Login**:
   - Send POST to `/api/auth/login`
   - Copy `token` from response
   - Save to environment variable

4. **Use Token**:
   - Add to Headers: `Authorization: Bearer {{token}}`
   - Or use Postman's Auth tab → Type: Bearer Token

---

## 🎯 Common Use Cases

### Use Case 1: Adding New Products

**Via Frontend:**
1. Login as ADMIN
2. Go to "Products" page
3. Click "Add Product" button
4. Fill in the form
5. Select Category and Supplier
6. Click "Save"

**Via API:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "sku": "MOU-001",
    "price": 25.99,
    "stockQuantity": 100,
    "reorderLevel": 20,
    "categoryId": 1,
    "supplierId": 1
  }'
```

### Use Case 2: Recording Stock Transactions

**Purchase (Stock In):**
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "transactionType": "PURCHASE",
    "quantity": 50,
    "unitPrice": 1400.00,
    "notes": "Monthly stock replenishment"
  }'
```

**Sale (Stock Out):**
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "transactionType": "SALE",
    "quantity": 5,
    "unitPrice": 1500.00,
    "notes": "Customer order #12345"
  }'
```

### Use Case 3: Monitoring Low Stock

**Via API:**
```bash
curl -X GET http://localhost:8080/api/products/low-stock \
  -H "Authorization: Bearer $TOKEN"
```

**Via Frontend:**
- Go to Dashboard
- Check "Low Stock Alerts" section
- Products below reorder level are highlighted in red

### Use Case 4: User Management (ADMIN only)

**Get All Users:**
```bash
curl -X GET http://localhost:8080/api/users/all \
  -H "Authorization: Bearer $TOKEN"
```

**Note**: Only ADMIN role can access user management endpoints.

---

## 🔒 Security Features

### Rate Limiting

The system implements brute force protection:
- **Max Failed Attempts**: 5
- **Lockout Duration**: 15 minutes
- **Automatic Reset**: After successful login

**Test it:**
```bash
# Try login with wrong password 6 times
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"wrong"}'
  echo "\nAttempt $i"
  sleep 1
done

# 6th attempt will be blocked
```

### Role-Based Access Control

| Feature | USER | ADMIN |
|---------|------|-------|
| View Products | ✅ | ✅ |
| Create Products | ✅ | ✅ |
| Edit Own Products | ✅ | ✅ |
| Delete Own Products | ✅ | ✅ |
| Delete Any Product | ❌ | ✅ |
| View All Users | ❌ | ✅ |
| Manage Transactions | ✅ | ✅ |

---

## 📊 Monitoring & Health Checks

### Health Endpoint

```bash
# Basic health
curl http://localhost:8080/actuator/health

# Detailed health (requires auth)
curl http://localhost:8080/actuator/health \
  -H "Authorization: Bearer $TOKEN"

# Liveness probe (for Kubernetes)
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

### Application Info

```bash
curl http://localhost:8080/actuator/info
```

Returns:
```json
{
  "app": {
    "name": "Stock Management System",
    "description": "Enterprise-grade Inventory Management System",
    "version": "1.0.0",
    "student": {
      "name": "Mehmet Taha Boynikoğlu",
      "id": "212 125 10 34"
    }
  }
}
```

### Metrics

```bash
# All available metrics
curl http://localhost:8080/actuator/metrics

# Specific metric (JVM memory)
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP requests
curl http://localhost:8080/actuator/metrics/http.server.requests

# Database connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

### Prometheus Metrics

```bash
# Prometheus format (for Grafana integration)
curl http://localhost:8080/actuator/prometheus
```

---

## 🐳 Docker Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f backend
docker-compose logs -f mysql

# Check status
docker-compose ps

# Execute commands in container
docker-compose exec backend sh
docker-compose exec mysql mysql -u root -p

# Restart a service
docker-compose restart backend

# Stop and remove all
docker-compose down

# Stop and remove with volumes
docker-compose down -v
```

---

## 🧪 Testing

### Run Unit Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests ProductServiceTest

# With coverage report
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Integration Tests

```bash
# Start test database
docker-compose -f docker-compose.test.yml up -d

# Run integration tests
./gradlew integrationTest

# Stop test database
docker-compose -f docker-compose.test.yml down
```

---

## 🔧 Troubleshooting

### Issue: "Port 8080 already in use"

**Solution:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use different port
./gradlew bootRun --args='--server.port=8081'
```

### Issue: "MySQL connection refused"

**Solution:**
```bash
# Check MySQL is running
docker-compose ps mysql

# Check MySQL logs
docker-compose logs mysql

# Restart MySQL
docker-compose restart mysql

# Test connection
mysql -h localhost -u root -p
```

### Issue: "JWT token expired"

**Solution:**
```bash
# Login again to get new token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Issue: "Frontend can't connect to backend"

**Solution:**
```bash
# Check CORS configuration
# Edit SecurityConfig.java line 132-137

# Or update frontend .env
cd frontend
echo "REACT_APP_API_URL=http://localhost:8080/api" > .env.development

# Restart frontend
npm start
```

---

## 📚 Additional Resources

- **Full Documentation**: [DOCUMENTATION.md](./DOCUMENTATION.md)
- **AWS Deployment**: [deployment/aws/AWS_DEPLOYMENT_GUIDE.md](./deployment/aws/AWS_DEPLOYMENT_GUIDE.md)
- **HTTPS Setup**: [docs/HTTPS_SSL_SETUP.md](./docs/HTTPS_SSL_SETUP.md)
- **Swagger UI**: http://localhost:8080/swagger-ui.html (when running)

---

## 💡 Tips & Best Practices

1. **Always use Swagger UI** for API testing - it's interactive and auto-documented
2. **Check Actuator health** before deployment: `curl http://localhost:8080/actuator/health`
3. **Use production profile** for deployment: `--spring.profiles.active=production`
4. **Keep JWT tokens secure** - never commit them to Git
5. **Monitor metrics** regularly using `/actuator/metrics`
6. **Test Docker build** before deploying: `docker-compose up`
7. **Run tests before commit**: `./gradlew test`
8. **Use environment variables** for sensitive data in production

---

**Last Updated:** December 2024  
**Version:** 1.0.0  
**Author:** Mehmet Taha Boynikoğlu (212 125 10 34)
