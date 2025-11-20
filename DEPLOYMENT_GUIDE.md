# 🚀 Deployment Guide

Bu rehber, Inventory Management System'in farklı ortamlara nasıl deploy edileceğini açıklar.

---

## 📋 İçindekiler

1. [Local Development](#local-development)
2. [Production Build](#production-build)
3. [AWS Deployment](#aws-deployment)
4. [Docker Deployment](#docker-deployment)
5. [Database Migration](#database-migration)
6. [Environment Configuration](#environment-configuration)

---

## 🖥️ Local Development

### Backend Development Mode
```bash
# Gradle ile development modda çalıştırma
./gradlew bootRun

# Hot reload için Spring DevTools aktif
# application.properties'de:
spring.devtools.restart.enabled=true
```

### Frontend Development Mode
```bash
cd frontend
npm start

# Port 3000'de çalışır, backend'e proxy yapar
```

---

## 📦 Production Build

### Backend Production Build
```bash
# Clean ve build
./gradlew clean build

# JAR dosyası oluşturulur:
# build/libs/StockManagement-0.0.1-SNAPSHOT.jar

# Production profili ile çalıştırma
java -jar build/libs/StockManagement-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### Frontend Production Build
```bash
cd frontend
npm run build

# Build dosyaları frontend/build/ dizinine oluşturulur
# Bu dosyalar static file server'a deploy edilebilir
```

---

## ☁️ AWS Deployment

### Prerequisites
- AWS Account
- AWS CLI installed
- EC2 instance (t2.micro minimum)
- RDS MySQL instance (optional)
- S3 bucket for frontend (optional)

### 1️⃣ EC2 Instance Setup

#### Instance Launch
```bash
# AMI: Ubuntu Server 20.04 LTS
# Instance Type: t2.micro (Free tier)
# Security Group: 
#   - SSH (22) from your IP
#   - HTTP (80) from anywhere
#   - HTTPS (443) from anywhere
#   - Custom TCP (8080) from anywhere
```

#### Connect to EC2
```bash
ssh -i your-key.pem ubuntu@your-ec2-public-ip
```

#### Install Java
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
java --version
```

#### Install MySQL
```bash
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql

# MySQL güvenlik ayarları
sudo mysql_secure_installation
```

#### Configure MySQL
```bash
sudo mysql -u root -p

CREATE DATABASE inventory_management_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

CREATE USER 'imsuser'@'localhost' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON inventory_management_db.* TO 'imsuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### Deploy Backend
```bash
# Local'den EC2'ye JAR transfer
scp -i your-key.pem build/libs/StockManagement-0.0.1-SNAPSHOT.jar \
  ubuntu@your-ec2-public-ip:~/

# EC2'de application.properties güncelle
nano application-prod.properties

# Uygulamayı çalıştır
java -jar StockManagement-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

#### Run as Service (Systemd)
```bash
# Service file oluştur
sudo nano /etc/systemd/system/ims-backend.service
```

```ini
[Unit]
Description=Inventory Management System Backend
After=syslog.target network.target

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /home/ubuntu/StockManagement-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# Service'i başlat
sudo systemctl daemon-reload
sudo systemctl enable ims-backend
sudo systemctl start ims-backend
sudo systemctl status ims-backend

# Logları izle
sudo journalctl -u ims-backend -f
```

### 2️⃣ RDS MySQL Setup (Optional)

#### Create RDS Instance
```bash
# AWS Console'dan:
# - Engine: MySQL 8.0
# - Instance: db.t3.micro (Free tier)
# - Storage: 20 GB
# - Public Access: Yes (development için)
# - Security Group: MySQL/Aurora (3306)
```

#### application-prod.properties Update
```properties
spring.datasource.url=jdbc:mysql://your-rds-endpoint:3306/inventory_management_db
spring.datasource.username=admin
spring.datasource.password=your-rds-password
```

### 3️⃣ Frontend Deployment

#### Option A: S3 Static Hosting

```bash
# Build frontend
cd frontend
npm run build

# S3 bucket oluştur
aws s3 mb s3://ims-frontend-bucket

# Static web hosting aktif et
aws s3 website s3://ims-frontend-bucket \
  --index-document index.html \
  --error-document index.html

# Dosyaları upload et
aws s3 sync build/ s3://ims-frontend-bucket --acl public-read

# CloudFront distribution oluştur (optional)
# SSL sertifikası için AWS Certificate Manager kullan
```

#### Option B: Nginx on EC2

```bash
# Nginx kurulumu
sudo apt install nginx -y

# Frontend build'i EC2'ye kopyala
scp -r frontend/build/* ubuntu@your-ec2-public-ip:/tmp/

# Nginx'e deploy
sudo mv /tmp/build/* /var/www/html/

# Nginx configuration
sudo nano /etc/nginx/sites-available/ims-frontend
```

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
# Nginx'i yeniden başlat
sudo ln -s /etc/nginx/sites-available/ims-frontend /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

---

## 🐳 Docker Deployment

### Dockerfile (Backend)
```dockerfile
# Backend Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/StockManagement-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Dockerfile (Frontend)
```dockerfile
# Frontend Dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: ims-mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: inventory_management_db
      MYSQL_USER: imsuser
      MYSQL_PASSWORD: imspassword
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - ims-network

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: ims-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/inventory_management_db
      SPRING_DATASOURCE_USERNAME: imsuser
      SPRING_DATASOURCE_PASSWORD: imspassword
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    networks:
      - ims-network

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: ims-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - ims-network

networks:
  ims-network:
    driver: bridge

volumes:
  mysql-data:
```

### Docker Commands
```bash
# Build images
docker-compose build

# Start containers
docker-compose up -d

# View logs
docker-compose logs -f

# Stop containers
docker-compose down

# Remove volumes
docker-compose down -v
```

---

## 🗄️ Database Migration

### Production Database Setup
```sql
-- Production database oluşturma
CREATE DATABASE IF NOT EXISTS inventory_management_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Kullanıcı oluşturma
CREATE USER 'ims_prod'@'%' IDENTIFIED BY 'secure_password_here';
GRANT ALL PRIVILEGES ON inventory_management_db.* TO 'ims_prod'@'%';
FLUSH PRIVILEGES;
```

### Backup Script
```bash
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"
DB_NAME="inventory_management_db"
DB_USER="root"
DB_PASS="password"

# Backup oluştur
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# 7 günden eski backupları sil
find $BACKUP_DIR -name "backup_*.sql" -mtime +7 -delete

echo "Backup completed: backup_$DATE.sql"
```

### Restore Script
```bash
#!/bin/bash
# restore.sh

DB_NAME="inventory_management_db"
DB_USER="root"
DB_PASS="password"
BACKUP_FILE=$1

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: ./restore.sh backup_file.sql"
    exit 1
fi

mysql -u $DB_USER -p$DB_PASS $DB_NAME < $BACKUP_FILE
echo "Database restored from: $BACKUP_FILE"
```

---

## ⚙️ Environment Configuration

### application-prod.properties
```properties
# Server Configuration
server.port=8080
server.compression.enabled=true

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management_db?useSSL=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.ims.stockmanagement=DEBUG
logging.file.name=/var/log/ims-backend.log

# CORS
cors.allowed-origins=https://your-domain.com
```

### Environment Variables
```bash
# .env file oluştur
cat > .env << EOF
DB_USERNAME=imsuser
DB_PASSWORD=secure_password
JWT_SECRET=your_super_secret_jwt_key_here_change_in_production
CORS_ORIGINS=https://your-domain.com
EOF

# Export environment variables
export $(cat .env | xargs)
```

---

## 🔒 SSL/HTTPS Setup

### Let's Encrypt with Certbot
```bash
# Certbot kurulumu
sudo apt install certbot python3-certbot-nginx -y

# SSL sertifikası al
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# Otomatik yenileme testi
sudo certbot renew --dry-run

# Cron job ekle (otomatik yenileme)
sudo crontab -e
# Ekle: 0 12 * * * /usr/bin/certbot renew --quiet
```

---

## 📊 Monitoring and Logging

### Application Monitoring
```bash
# Spring Boot Actuator endpoints aktif et
# application-prod.properties:
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### Log Rotation
```bash
# /etc/logrotate.d/ims-backend
/var/log/ims-backend.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 0644 ubuntu ubuntu
}
```

---

## ✅ Deployment Checklist

### Pre-Deployment
- [ ] Tüm testler geçiyor
- [ ] Database migration scriptleri hazır
- [ ] Environment variables ayarlandı
- [ ] SSL sertifikaları yapılandırıldı
- [ ] Backup stratejisi belirlendi
- [ ] Monitoring araçları kuruldu

### Deployment
- [ ] Database backup alındı
- [ ] Backend deploy edildi
- [ ] Frontend deploy edildi
- [ ] Health check yapıldı
- [ ] API endpoints test edildi
- [ ] Frontend-Backend bağlantısı kontrol edildi

### Post-Deployment
- [ ] Loglar kontrol edildi
- [ ] Performance test yapıldı
- [ ] Security scan yapıldı
- [ ] Documentation güncellendi
- [ ] Takıma bilgi verildi

---

## 🆘 Troubleshooting

### Backend Won't Start
```bash
# Logları kontrol et
sudo journalctl -u ims-backend -n 100

# Port kullanımı kontrol et
sudo lsof -i :8080

# Java process kontrol et
ps aux | grep java
```

### Database Connection Error
```bash
# MySQL servis durumu
sudo systemctl status mysql

# Bağlantı testi
mysql -u imsuser -p -h localhost inventory_management_db

# Firewall kontrol
sudo ufw status
```

### Frontend Not Loading
```bash
# Nginx status
sudo systemctl status nginx

# Nginx configuration test
sudo nginx -t

# Access logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

---

## 📞 Support

Deployment ile ilgili sorunlar için:
- GitHub Issues: [Stock_Management/issues](https://github.com/mrblackcoder/Stock_Management/issues)
- Email: mehmet.taha.boynikoglu@example.com

---

**Last Updated:** 2024-11-20
**Version:** 1.0.0

