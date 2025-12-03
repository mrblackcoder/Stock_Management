# 🚀 Stock Management System - Deployment Guide

## 📋 Table of Contents
1. [Local Development Setup](#local-development-setup)
2. [Docker Deployment](#docker-deployment)
3. [AWS Deployment](#aws-deployment)
4. [Environment Variables](#environment-variables)
5. [Security Considerations](#security-considerations)
6. [Troubleshooting](#troubleshooting)

---

## 🏠 Local Development Setup

### Prerequisites
- Java 21 or higher
- Node.js 18+ and npm
- Docker (recommended for MySQL)
- Git

### Step 1: Clone Repository
```bash
git clone https://github.com/mrblackcoder/Stock_Management.git
cd Stock_Management
```

### Step 2: Start MySQL with Docker
```bash
# Start MySQL container
docker run --name stock-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=inventory_management_db \
  -p 3306:3306 \
  -d mysql:8.0

# Wait 10 seconds for MySQL to initialize
sleep 10

# Verify MySQL is running
docker ps | grep stock-mysql
```

**Alternative: Stop MySQL container**
```bash
docker stop stock-mysql
```

**Alternative: Start existing container**
```bash
docker start stock-mysql
```

**Alternative: Connect to MySQL**
```bash
docker exec -it stock-mysql mysql -uroot -proot
```

### Step 3: Configure Application
Edit `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root

# JWT Secret (CHANGE IN PRODUCTION!)
jwt.secret=${JWT_SECRET:your-very-long-secret-key-here}

# Admin Password (CHANGE IN PRODUCTION!)
admin.default.password=${ADMIN_PASSWORD:Admin@123!Secure}
```

### Step 4: Start Backend
```bash
# Option 1: Using Gradle Wrapper (Recommended)
./gradlew bootRun

# Option 2: Build JAR and run
./gradlew clean bootJar
java -jar build/libs/StockManagement-*.jar

# Backend will start on: http://localhost:8080
```

### Step 5: Start Frontend
```bash
cd frontend
npm install
npm start

# Frontend will start on: http://localhost:3000
```

### Step 6: Access Application
- **React SPA**: http://localhost:3000
- **Thymeleaf Login**: http://localhost:8080/login
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

**Default Credentials:**
- Username: `admin`
- Password: `Admin@123!Secure`

---

## 🐳 Docker Deployment (Full Stack)

### Option 1: Docker Compose (Recommended)

Create `docker-compose.yml`:
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: stock-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD:-root}
      MYSQL_DATABASE: inventory_management_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - stock-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: .
    container_name: stock-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/inventory_management_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-root}
      JWT_SECRET: ${JWT_SECRET:-default-development-secret-change-in-production}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD:-Admin@123!Secure}
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - stock-network

  frontend:
    build: ./frontend
    container_name: stock-frontend
    ports:
      - "3000:3000"
    environment:
      REACT_APP_API_URL: http://localhost:8080
    depends_on:
      - backend
    networks:
      - stock-network

volumes:
  mysql_data:

networks:
  stock-network:
    driver: bridge
```

**Start all services:**
```bash
docker-compose up -d
```

**Stop all services:**
```bash
docker-compose down
```

**View logs:**
```bash
docker-compose logs -f backend
```

---

## ☁️ AWS Deployment

### Architecture Overview
```
Internet → AWS ALB → EC2 (Backend) → RDS MySQL
                  → S3 (Frontend Static Files)
```

### Step 1: Setup RDS MySQL Database

1. **Create RDS Instance:**
   - Go to AWS Console → RDS → Create Database
   - Engine: MySQL 8.0
   - Template: Free Tier (for testing) or Production
   - DB Instance Identifier: `stock-management-db`
   - Master Username: `admin`
   - Master Password: (save this securely!)
   - DB Instance Class: `db.t3.micro` (free tier)
   - Storage: 20 GB
   - Enable automated backups
   - Security Group: Allow inbound from EC2 security group (port 3306)

2. **Note the Endpoint:**
   ```
   stock-management-db.xxxxx.us-east-1.rds.amazonaws.com:3306
   ```

### Step 2: Deploy Backend to EC2

1. **Launch EC2 Instance:**
   - AMI: Amazon Linux 2023 or Ubuntu 22.04
   - Instance Type: `t2.micro` (free tier) or `t3.medium` (recommended)
   - Key Pair: Create and download `.pem` file
   - Security Group: 
     - SSH (22) from your IP
     - HTTP (80) from anywhere
     - HTTPS (443) from anywhere
     - Custom TCP (8080) from anywhere (or ALB security group)

2. **Connect to EC2:**
   ```bash
   ssh -i "your-key.pem" ec2-user@your-ec2-public-ip
   ```

3. **Install Dependencies:**
   ```bash
   # Install Java 21
   sudo yum install -y java-21-amazon-corretto-devel
   
   # Verify Java installation
   java -version
   
   # Install Git
   sudo yum install -y git
   ```

4. **Clone and Configure:**
   ```bash
   # Clone repository
   git clone https://github.com/mrblackcoder/Stock_Management.git
   cd Stock_Management
   
   # Create environment file
   cat > .env << EOF
   export DB_PASSWORD=your-rds-password
   export JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
   export ADMIN_PASSWORD=YourSecureAdminPassword123!
   export SPRING_DATASOURCE_URL=jdbc:mysql://your-rds-endpoint:3306/inventory_management_db
   EOF
   
   # Load environment variables
   source .env
   ```

5. **Build and Run:**
   ```bash
   # Build JAR
   ./gradlew clean bootJar
   
   # Run as background service
   nohup java -jar build/libs/StockManagement-*.jar \
     --spring.datasource.url=$SPRING_DATASOURCE_URL \
     --spring.datasource.password=$DB_PASSWORD \
     --jwt.secret=$JWT_SECRET \
     --admin.default.password=$ADMIN_PASSWORD \
     > backend.log 2>&1 &
   
   # Check if running
   curl http://localhost:8080/actuator/health
   ```

6. **Setup Systemd Service (Production):**
   ```bash
   sudo nano /etc/systemd/system/stock-backend.service
   ```
   
   ```ini
   [Unit]
   Description=Stock Management Backend
   After=network.target
   
   [Service]
   Type=simple
   User=ec2-user
   WorkingDirectory=/home/ec2-user/Stock_Management
   EnvironmentFile=/home/ec2-user/Stock_Management/.env
   ExecStart=/usr/bin/java -jar /home/ec2-user/Stock_Management/build/libs/StockManagement-1.0.0.jar
   Restart=on-failure
   RestartSec=10
   StandardOutput=journal
   StandardError=journal
   
   [Install]
   WantedBy=multi-user.target
   ```
   
   ```bash
   # Enable and start service
   sudo systemctl daemon-reload
   sudo systemctl enable stock-backend
   sudo systemctl start stock-backend
   sudo systemctl status stock-backend
   ```

### Step 3: Deploy Frontend to S3 + CloudFront

1. **Build React App:**
   ```bash
   cd frontend
   npm install
   npm run build
   ```

2. **Create S3 Bucket:**
   - Go to S3 → Create Bucket
   - Bucket Name: `stock-management-frontend-yourname`
   - Region: Same as EC2
   - Uncheck "Block all public access"
   - Enable Static Website Hosting

3. **Upload Build Files:**
   ```bash
   aws s3 sync build/ s3://stock-management-frontend-yourname/ --acl public-read
   ```

4. **Configure Bucket Policy:**
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "PublicReadGetObject",
         "Effect": "Allow",
         "Principal": "*",
         "Action": "s3:GetObject",
         "Resource": "arn:aws:s3:::stock-management-frontend-yourname/*"
       }
     ]
   }
   ```

5. **Setup CloudFront (Optional but Recommended):**
   - Create CloudFront Distribution
   - Origin Domain: Your S3 bucket
   - Default Root Object: `index.html`
   - SSL Certificate: Use AWS Certificate Manager

6. **Update Frontend API URL:**
   
   Create `.env.production` in frontend folder:
   ```
   REACT_APP_API_URL=http://your-ec2-public-ip:8080
   ```
   
   Rebuild and redeploy:
   ```bash
   npm run build
   aws s3 sync build/ s3://stock-management-frontend-yourname/ --acl public-read
   ```

### Step 4: Configure Application Load Balancer (Optional)

1. Create ALB targeting EC2 instance on port 8080
2. Configure SSL certificate from ACM
3. Update frontend to use ALB URL instead of EC2 IP

---

## 🔐 Environment Variables

### Required for Production

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database JDBC URL | `jdbc:mysql://rds-endpoint:3306/db` |
| `DB_PASSWORD` | MySQL root password | `SecurePassword123!` |
| `JWT_SECRET` | JWT signing secret (256+ chars) | Generate with `openssl rand -base64 64` |
| `ADMIN_PASSWORD` | Admin user password | `Admin@SecurePass123!` |

### Optional

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Backend server port | `8080` |
| `JWT_EXPIRATION` | Access token expiration (ms) | `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | `604800000` (7 days) |

### Setting Environment Variables

**Linux/Mac:**
```bash
export JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
export DB_PASSWORD=YourSecurePassword
```

**Windows PowerShell:**
```powershell
$env:JWT_SECRET = "your-secret-key"
$env:DB_PASSWORD = "your-password"
```

**Docker:**
```bash
docker run -e JWT_SECRET="secret" -e DB_PASSWORD="pass" ...
```

**AWS EC2 (Persistent):**
```bash
# Add to ~/.bashrc or ~/.bash_profile
echo 'export JWT_SECRET="your-secret"' >> ~/.bashrc
source ~/.bashrc
```

---

## 🔒 Security Considerations

### ⚠️ CRITICAL Security Checklist

- [ ] **Change default passwords** before deployment
- [ ] **Generate strong JWT secret** (256+ characters)
- [ ] **Use environment variables** for all secrets
- [ ] **Never commit** `.env` files or secrets to Git
- [ ] **Enable HTTPS** with valid SSL certificate
- [ ] **Configure CORS** for production domains only
- [ ] **Use RDS encryption** at rest and in transit
- [ ] **Enable AWS CloudWatch** for monitoring
- [ ] **Setup automated backups** for RDS
- [ ] **Use IAM roles** instead of hardcoded AWS credentials
- [ ] **Enable AWS WAF** for DDoS protection
- [ ] **Configure security groups** with least privilege

### Password Requirements
- **Minimum 12 characters**
- **Mix of uppercase, lowercase, numbers, symbols**
- **No dictionary words**
- **Rotate every 90 days**

### JWT Secret Generation
```bash
# Generate secure 512-bit secret
openssl rand -base64 64 | tr -d '\n'
```

### Database Security
- Use RDS encryption
- Enable SSL for database connections
- Restrict access to specific IP ranges
- Regular automated backups
- Enable audit logging

---

## 🛠️ Troubleshooting

### Backend won't start
```bash
# Check logs
tail -f backend.log

# Check if port 8080 is in use
lsof -i :8080
kill -9 <PID>

# Check MySQL connection
docker exec -it stock-mysql mysql -uroot -proot -e "SHOW DATABASES;"
```

### Frontend can't connect to backend
```bash
# Check CORS configuration in SecurityConfig.java
# Verify API URL in frontend .env file
# Check browser console for errors
```

### Database connection refused
```bash
# Check MySQL is running
docker ps | grep mysql

# Test connection
mysql -h localhost -u root -proot -e "SELECT 1;"

# Check firewall rules (AWS Security Groups)
```

### Out of memory on EC2
```bash
# Increase JVM heap size
java -Xmx1024m -jar StockManagement.jar
```

---

## 📞 Support

- **GitHub Issues**: https://github.com/mrblackcoder/Stock_Management/issues
- **Student**: Mehmet Taha Boynikoğlu (212 125 10 34)
- **University**: Fatih Sultan Mehmet Vakıf Üniversitesi

---

## 📄 License

This project is part of academic coursework for Web Design and Programming course.

---

**Last Updated**: December 2025
**Version**: 1.0.0
