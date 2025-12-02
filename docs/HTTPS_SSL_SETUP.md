# HTTPS/SSL Configuration Guide

## Development Environment (Self-Signed Certificate)

### 1. Generate Self-Signed Certificate

```bash
# Navigate to src/main/resources
cd src/main/resources

# Generate PKCS12 keystore
keytool -genkeypair \
    -alias stock-management \
    -keyalg RSA \
    -keysize 2048 \
    -storetype PKCS12 \
    -keystore keystore.p12 \
    -validity 365 \
    -dname "CN=localhost, OU=Development, O=IMS, L=Istanbul, ST=Istanbul, C=TR"
```

**Prompts:**
- Enter keystore password: `changeit` (or your choice)
- Re-enter password: `changeit`

### 2. Update application.properties

```properties
# HTTPS Configuration (Development)
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=stock-management

# HTTP to HTTPS redirect (optional)
server.http.port=8080
```

### 3. Update SecurityConfig

Uncomment HSTS header in SecurityConfig.java:

```java
// Force HTTPS (HSTS)
.httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000)  // 1 year
)
```

### 4. Test HTTPS

```bash
# Start application
./gradlew bootRun

# Access via HTTPS
curl -k https://localhost:8443/api/auth/login

# Update frontend API URL
# frontend/.env
VITE_API_URL=https://localhost:8443/api
```

**Note:** Browser will show "Not Secure" warning for self-signed certificates. This is expected in development.

---

## Production Environment (Let's Encrypt + AWS)

### Option 1: Using AWS Certificate Manager (ACM)

#### 1. Request Certificate in ACM

```bash
# Request certificate
aws acm request-certificate \
    --domain-name yourdomain.com \
    --subject-alternative-names www.yourdomain.com api.yourdomain.com \
    --validation-method DNS \
    --region us-east-1
```

#### 2. Validate Domain

1. Go to AWS Console → Certificate Manager
2. Copy CNAME records
3. Add to your DNS provider (Route 53, GoDaddy, etc.)
4. Wait for validation (5-30 minutes)

#### 3. Attach to Load Balancer

```bash
# Elastic Beanstalk with HTTPS
eb create inventory-api-env \
    --elb-type application \
    --https-certificate-arn arn:aws:acm:us-east-1:123456789:certificate/xyz
```

#### 4. Update Application

```properties
# application-production.properties
server.port=5000
server.ssl.enabled=false  # Load balancer handles SSL

# CORS for production
cors.allowed-origins=https://yourdomain.com,https://www.yourdomain.com
```

### Option 2: Let's Encrypt (Manual Setup on EC2)

#### 1. Install Certbot

```bash
# SSH to EC2 instance
ssh -i your-key.pem ec2-user@your-ec2-ip

# Install Certbot
sudo yum install certbot -y
# or for Ubuntu
sudo apt-get update
sudo apt-get install certbot -y
```

#### 2. Stop Application

```bash
# Stop Spring Boot app temporarily
sudo systemctl stop spring-boot-app
```

#### 3. Request Certificate

```bash
# Request certificate (requires port 80 open)
sudo certbot certonly --standalone \
    -d api.yourdomain.com \
    -d yourdomain.com \
    --agree-tos \
    --email your-email@example.com
```

Certificates will be saved to:
- Certificate: `/etc/letsencrypt/live/api.yourdomain.com/fullchain.pem`
- Private Key: `/etc/letsencrypt/live/api.yourdomain.com/privkey.pem`

#### 4. Convert to PKCS12 Format

```bash
# Convert PEM to PKCS12
sudo openssl pkcs12 -export \
    -in /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem \
    -inkey /etc/letsencrypt/live/api.yourdomain.com/privkey.pem \
    -out /etc/ssl/keystore.p12 \
    -name stock-management \
    -password pass:YourStrongPassword123
```

#### 5. Update application.properties

```properties
# Production HTTPS Configuration
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=file:/etc/ssl/keystore.p12
server.ssl.key-store-password=YourStrongPassword123
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=stock-management

# Force HTTPS
server.require-ssl=true
```

#### 6. Auto-Renewal

```bash
# Test renewal
sudo certbot renew --dry-run

# Create renewal script
sudo nano /etc/cron.monthly/renew-cert.sh
```

```bash
#!/bin/bash
certbot renew --quiet --post-hook "systemctl restart spring-boot-app"

# Convert to PKCS12
openssl pkcs12 -export \
    -in /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem \
    -inkey /etc/letsencrypt/live/api.yourdomain.com/privkey.pem \
    -out /etc/ssl/keystore.p12 \
    -name stock-management \
    -password pass:YourStrongPassword123
```

```bash
# Make executable
sudo chmod +x /etc/cron.monthly/renew-cert.sh
```

---

## Security Best Practices

### 1. Strong Cipher Suites

```properties
# application.properties
server.ssl.ciphers=TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
server.ssl.protocol=TLS
```

### 2. HTTP to HTTPS Redirect

Add to SecurityConfig.java:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // ... other config

        // Force HTTPS
        .requiresChannel(channel -> channel
            .anyRequest().requiresSecure()
        );

    return http.build();
}
```

### 3. Secure Cookie Settings

```properties
# application.properties
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
```

### 4. HSTS Header

Already configured in SecurityConfig:

```java
.httpStrictTransportSecurity(hsts -> hsts
    .includeSubDomains(true)
    .maxAgeInSeconds(31536000)  // 1 year
)
```

---

## Troubleshooting

### Issue: "Certificate not trusted"

**Solution:** For production, use ACM or Let's Encrypt. For development, add exception in browser.

### Issue: "Connection refused on port 8443"

**Solution:**
```bash
# Check if port is open
sudo netstat -tulpn | grep 8443

# Open port in firewall
sudo ufw allow 8443/tcp

# AWS Security Group
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxx \
    --protocol tcp \
    --port 8443 \
    --cidr 0.0.0.0/0
```

### Issue: "SSL handshake failed"

**Solution:** Check cipher suites and TLS version:

```bash
# Test SSL connection
openssl s_client -connect localhost:8443 -tls1_2

# Check certificate
openssl s_client -connect localhost:8443 -showcerts
```

---

## Testing HTTPS

### 1. cURL Test

```bash
# Development (self-signed)
curl -k https://localhost:8443/api/auth/login

# Production
curl https://api.yourdomain.com/api/auth/login
```

### 2. Browser Test

1. Navigate to `https://localhost:8443`
2. Accept security warning (dev only)
3. Verify padlock icon

### 3. SSL Labs Test

For production domains:
https://www.ssllabs.com/ssltest/analyze.html?d=yourdomain.com

---

## Frontend Configuration

### Development

```javascript
// frontend/.env.development
VITE_API_URL=https://localhost:8443/api
```

### Production

```javascript
// frontend/.env.production
VITE_API_URL=https://api.yourdomain.com/api
```

---

**Last Updated:** December 2024  
**Maintained by:** Mehmet Taha Boynikoğlu
