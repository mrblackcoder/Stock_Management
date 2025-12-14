#!/bin/bash
# EC2 Setup Script for Stock Management Application
# Run this script on a fresh Ubuntu 22.04/24.04 EC2 instance

set -e

echo "=========================================="
echo "Stock Management - EC2 Setup Script"
echo "=========================================="

# Update system
echo "[1/8] Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install Java 21
echo "[2/8] Installing Java 21..."
sudo apt install -y openjdk-21-jdk

# Install Nginx
echo "[3/8] Installing Nginx..."
sudo apt install -y nginx

# Install Node.js 20 (for serve if needed)
echo "[4/8] Installing Node.js 20..."
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# Create application directories
echo "[5/8] Creating application directories..."
sudo mkdir -p /opt/stockmanagement
sudo mkdir -p /var/www/stockmanagement
sudo mkdir -p /var/log/stockmanagement
sudo chown -R ubuntu:ubuntu /opt/stockmanagement
sudo chown -R www-data:www-data /var/www/stockmanagement
sudo chown -R ubuntu:ubuntu /var/log/stockmanagement

# Create .env file template
echo "[6/8] Creating environment file template..."
cat > /opt/stockmanagement/.env << 'EOF'
# Database Configuration
DB_HOST=your-rds-endpoint.amazonaws.com
DB_PORT=3306
DB_NAME=stock_management
DB_USERNAME=admin
DB_PASSWORD=your-password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-at-least-256-bits-long-for-security
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Admin Configuration
ADMIN_PASSWORD=Admin@123!Secure

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://YOUR_EC2_IP
EOF

echo "Please edit /opt/stockmanagement/.env with your actual values!"

# Setup Nginx
echo "[7/8] Configuring Nginx..."
sudo rm -f /etc/nginx/sites-enabled/default

# Copy nginx config (you'll need to do this manually or via deployment)
cat > /tmp/stockmanagement.conf << 'NGINX_EOF'
server {
    listen 80;
    server_name _;
    
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    
    gzip on;
    gzip_types text/plain text/css text/javascript application/javascript application/json;
    
    location / {
        root /var/www/stockmanagement;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /actuator/ {
        proxy_pass http://127.0.0.1:8080/actuator/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
NGINX_EOF

sudo mv /tmp/stockmanagement.conf /etc/nginx/sites-available/stockmanagement
sudo ln -sf /etc/nginx/sites-available/stockmanagement /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx

# Setup systemd service
echo "[8/8] Setting up systemd service..."
cat > /tmp/stockmanagement-backend.service << 'SERVICE_EOF'
[Unit]
Description=Stock Management Backend
After=network.target

[Service]
Type=simple
User=ubuntu
Group=ubuntu
EnvironmentFile=/opt/stockmanagement/.env
Environment="JAVA_OPTS=-Xms256m -Xmx512m"
ExecStart=/usr/bin/java $JAVA_OPTS -jar /opt/stockmanagement/app.jar --spring.profiles.active=production
Restart=on-failure
RestartSec=10
StandardOutput=append:/var/log/stockmanagement/backend.log
StandardError=append:/var/log/stockmanagement/backend-error.log

[Install]
WantedBy=multi-user.target
SERVICE_EOF

sudo mv /tmp/stockmanagement-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable stockmanagement-backend

echo "=========================================="
echo "Setup complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Edit /opt/stockmanagement/.env with your database credentials"
echo "2. Copy your app.jar to /opt/stockmanagement/"
echo "3. Copy your frontend build to /var/www/stockmanagement/"
echo "4. Run: sudo systemctl start stockmanagement-backend"
echo "5. Access your app at http://YOUR_EC2_IP"
echo ""
echo "Useful commands:"
echo "  sudo systemctl status stockmanagement-backend"
echo "  sudo tail -f /var/log/stockmanagement/backend.log"
echo "  sudo nginx -t && sudo systemctl reload nginx"
