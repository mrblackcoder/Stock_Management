# AWS Deployment Guide
## Stock Management System - Cloud Deployment Documentation

**Project:** Inventory Management System  
**Student:** Mehmet Taha Boynikoğlu (212 125 10 34)  
**Date:** December 2024

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [AWS Services Architecture](#aws-services-architecture)
4. [Step-by-Step Deployment](#step-by-step-deployment)
5. [Configuration](#configuration)
6. [Security Best Practices](#security-best-practices)
7. [Monitoring & Maintenance](#monitoring--maintenance)
8. [Cost Estimation](#cost-estimation)
9. [Troubleshooting](#troubleshooting)

---

## Overview

This guide provides comprehensive instructions for deploying the Stock Management System on Amazon Web Services (AWS). The deployment utilizes modern cloud infrastructure to ensure scalability, reliability, and security.

### Deployment Architecture

```
Internet → Route 53 (DNS) → Application Load Balancer
                ↓
        Elastic Beanstalk (Backend)
                ↓
        RDS MySQL (Database)
                ↓
        S3 (Frontend Static Hosting)
```

---

## Prerequisites

### Required AWS Services

- **AWS Account** with appropriate permissions
- **AWS CLI** installed and configured
- **EB CLI** (Elastic Beanstalk Command Line Interface)
- **Domain Name** (optional, for custom domain)

### Local Requirements

- Java 21 JDK
- Node.js 18+
- Git
- MySQL Client (for database management)

### AWS Account Permissions

Ensure your IAM user has the following permissions:
- EC2 Full Access
- RDS Full Access
- S3 Full Access
- Elastic Beanstalk Full Access
- CloudWatch Logs Access
- IAM Role Creation

---

## AWS Services Architecture

### 1. Amazon RDS (Relational Database Service)

**Purpose:** Managed MySQL database for application data

**Configuration:**
- **Engine:** MySQL 8.0
- **Instance Type:** db.t3.micro (Free Tier) / db.t3.small (Production)
- **Storage:** 20 GB General Purpose (SSD)
- **Multi-AZ:** Yes (for production)
- **Backup Retention:** 7 days
- **Automated Backups:** Enabled

**Security:**
- VPC Security Group restricting access
- Encryption at rest enabled
- SSL/TLS for data in transit

### 2. AWS Elastic Beanstalk

**Purpose:** Managed platform for Spring Boot backend

**Configuration:**
- **Platform:** Java 21 with Corretto
- **Environment Type:** Load Balanced
- **Instance Type:** t3.micro (Free Tier) / t3.small (Production)
- **Auto Scaling:** 1-4 instances
- **Load Balancer:** Application Load Balancer (ALB)

**Benefits:**
- Automatic deployment
- Built-in monitoring
- Auto-scaling capabilities
- Easy rollback

### 3. Amazon S3 (Simple Storage Service)

**Purpose:** Static website hosting for React frontend

**Configuration:**
- **Bucket Name:** stock-management-frontend
- **Static Website Hosting:** Enabled
- **Public Access:** Configured for web hosting
- **CORS:** Configured for API calls

**Optional Enhancement:**
- Amazon CloudFront for CDN (faster global delivery)

### 4. Amazon Route 53 (Optional)

**Purpose:** DNS management and custom domain

**Configuration:**
- Domain registration/management
- Hosted zones for DNS records
- Health checks
- Routing policies

### 5. Amazon CloudWatch

**Purpose:** Monitoring, logging, and alerts

**Configuration:**
- Application logs
- Performance metrics
- Custom dashboards
- Alarm notifications

---

## Step-by-Step Deployment

### Phase 1: Database Setup (RDS)

#### 1.1 Create RDS MySQL Instance

**Via AWS Console:**

1. Navigate to RDS Dashboard
2. Click "Create database"
3. Configuration:
   ```
   Engine: MySQL 8.0.35
   Template: Production / Dev/Test
   DB Instance Identifier: inventory-management-db
   Master Username: admin
   Master Password: [Strong Password - Save This!]
   Instance Type: db.t3.micro
   Storage: 20 GB gp3
   VPC: Default VPC
   Public Access: No (Yes for initial setup, change to No after)
   Database Name: inventory_management_db
   ```

4. Security Group:
   - Create new: `rds-stock-management-sg`
   - Inbound Rule: MySQL/Aurora (3306) from Elastic Beanstalk Security Group

5. Click "Create database"

**Via AWS CLI:**

```bash
aws rds create-db-instance \
    --db-instance-identifier inventory-management-db \
    --db-instance-class db.t3.micro \
    --engine mysql \
    --engine-version 8.0.35 \
    --master-username admin \
    --master-password YOUR_STRONG_PASSWORD \
    --allocated-storage 20 \
    --storage-type gp3 \
    --vpc-security-group-ids sg-xxxxxxxx \
    --db-name inventory_management_db \
    --backup-retention-period 7 \
    --no-multi-az \
    --publicly-accessible
```

#### 1.2 Configure Database

Wait for RDS instance to be available (5-10 minutes), then:

1. Get RDS Endpoint:
   ```bash
   aws rds describe-db-instances \
       --db-instance-identifier inventory-management-db \
       --query "DBInstances[0].Endpoint.Address" \
       --output text
   ```

2. Connect and initialize:
   ```bash
   mysql -h <RDS_ENDPOINT> -u admin -p inventory_management_db
   ```

3. Database will be auto-created by Hibernate on first run

---

### Phase 2: Backend Deployment (Elastic Beanstalk)

#### 2.1 Prepare Application

1. Update `application.properties`:

```properties
# AWS RDS Configuration
spring.datasource.url=jdbc:mysql://<RDS_ENDPOINT>:3306/inventory_management_db?useSSL=true&requireSSL=true
spring.datasource.username=admin
spring.datasource.password=${DB_PASSWORD}

# Production Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Server Configuration
server.port=5000
server.compression.enabled=true

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.access-token.expiration=900000
jwt.refresh-token.expiration=604800000

# Logging
logging.level.root=INFO
logging.level.com.ims.stockmanagement=INFO
```

2. Build JAR file:

```bash
cd /path/to/Stock_Management
./gradlew clean bootJar
```

JAR location: `build/libs/StockManagement-1.0.0.jar`

#### 2.2 Initialize Elastic Beanstalk

```bash
# Install EB CLI
pip install awsebcli --upgrade --user

# Initialize EB application
cd /path/to/Stock_Management
eb init

# Follow prompts:
# - Region: Choose your region (e.g., us-east-1)
# - Application name: stock-management-backend
# - Platform: Java
# - Platform version: Corretto 21
# - SSH: Yes (create new keypair)
```

#### 2.3 Create Environment

```bash
eb create stock-management-prod \
    --instance-type t3.small \
    --platform "Java 21" \
    --envvars DB_PASSWORD=YOUR_RDS_PASSWORD,JWT_SECRET=YOUR_JWT_SECRET \
    --database.engine mysql \
    --database.version 8.0
```

**Environment Variables to Set:**

```bash
eb setenv \
    DB_PASSWORD="your_rds_password" \
    JWT_SECRET="your_jwt_secret_key" \
    SPRING_PROFILES_ACTIVE="prod"
```

#### 2.4 Deploy Application

```bash
# Deploy JAR file
eb deploy

# Monitor deployment
eb logs --stream
```

#### 2.5 Configure Load Balancer

1. Navigate to EC2 → Load Balancers
2. Select Elastic Beanstalk Load Balancer
3. Configure:
   - **Health Check Path:** `/actuator/health`
   - **Health Check Interval:** 30 seconds
   - **Timeout:** 5 seconds
   - **Healthy Threshold:** 2
   - **Unhealthy Threshold:** 5

4. Add HTTPS Listener (if using SSL certificate):
   ```
   Port: 443
   Protocol: HTTPS
   Certificate: Request from ACM or upload
   ```

#### 2.6 Verify Backend

```bash
# Get EB environment URL
eb status

# Test health endpoint
curl http://your-eb-url.elasticbeanstalk.com/actuator/health

# Expected Response:
# {"status":"UP"}
```

---

### Phase 3: Frontend Deployment (S3 + CloudFront)

#### 3.1 Build React Application

1. Update API endpoint in `ApiService.js`:

```javascript
const BASE_URL = process.env.REACT_APP_API_URL || 
    'http://your-eb-url.elasticbeanstalk.com';
```

2. Create `.env.production`:

```env
REACT_APP_API_URL=http://your-eb-url.elasticbeanstalk.com
```

3. Build production bundle:

```bash
cd frontend
npm install
npm run build
```

Build output: `frontend/build/`

#### 3.2 Create S3 Bucket

**Via AWS Console:**

1. Navigate to S3
2. Create bucket:
   ```
   Bucket name: stock-management-frontend-prod
   Region: Same as Elastic Beanstalk
   Block Public Access: DISABLED (for website hosting)
   Versioning: Enabled
   ```

3. Enable Static Website Hosting:
   - Properties → Static website hosting → Enable
   - Index document: `index.html`
   - Error document: `index.html`

4. Bucket Policy (replace BUCKET_NAME):

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::BUCKET_NAME/*"
        }
    ]
}
```

5. CORS Configuration:

```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "HEAD", "POST", "PUT", "DELETE"],
        "AllowedOrigins": ["*"],
        "ExposeHeaders": ["ETag"]
    }
]
```

**Via AWS CLI:**

```bash
# Create bucket
aws s3 mb s3://stock-management-frontend-prod

# Enable website hosting
aws s3 website s3://stock-management-frontend-prod \
    --index-document index.html \
    --error-document index.html

# Set bucket policy
aws s3api put-bucket-policy \
    --bucket stock-management-frontend-prod \
    --policy file://bucket-policy.json
```

#### 3.3 Upload Frontend

```bash
cd frontend/build

# Upload all files
aws s3 sync . s3://stock-management-frontend-prod \
    --delete \
    --cache-control "max-age=31536000, public" \
    --exclude "*.html" \
    --exclude "service-worker.js"

# Upload HTML files with no-cache
aws s3 sync . s3://stock-management-frontend-prod \
    --delete \
    --cache-control "no-cache, no-store, must-revalidate" \
    --include "*.html" \
    --include "service-worker.js"
```

#### 3.4 Get S3 Website URL

```bash
# Get website endpoint
aws s3api get-bucket-website \
    --bucket stock-management-frontend-prod \
    --query '[WebsiteURL]' \
    --output text
```

URL Format: `http://stock-management-frontend-prod.s3-website-us-east-1.amazonaws.com`

---

### Phase 4: Domain Configuration (Optional)

#### 4.1 Register Domain (Route 53)

1. Navigate to Route 53
2. Register domain or transfer existing
3. Create Hosted Zone

#### 4.2 Configure DNS Records

**Backend (Elastic Beanstalk):**

```
Type: CNAME
Name: api.yourdomain.com
Value: your-eb-url.elasticbeanstalk.com
TTL: 300
```

**Frontend (S3):**

```
Type: CNAME
Name: www.yourdomain.com
Value: stock-management-frontend-prod.s3-website-us-east-1.amazonaws.com
TTL: 300
```

#### 4.3 SSL Certificate (ACM)

1. Request certificate in AWS Certificate Manager
2. Domain: `*.yourdomain.com`
3. Validation: DNS validation
4. Add validation CNAME to Route 53
5. Wait for certificate approval

6. Attach certificate to Load Balancer:
   - EC2 → Load Balancers
   - Add HTTPS listener with ACM certificate

---

### Phase 5: CloudFront Setup (Optional - Recommended)

#### 5.1 Create CloudFront Distribution

**For Frontend:**

```
Origin Domain: stock-management-frontend-prod.s3-website-us-east-1.amazonaws.com
Origin Protocol Policy: HTTP Only
Viewer Protocol Policy: Redirect HTTP to HTTPS
Compress Objects Automatically: Yes
Price Class: Use Only North America and Europe
Alternate Domain Names: www.yourdomain.com
SSL Certificate: Your ACM certificate
Default Root Object: index.html
```

**Custom Error Responses:**
```
HTTP Error Code: 403
Error Caching Minimum TTL: 0
Response Page Path: /index.html
HTTP Response Code: 200
```

```
HTTP Error Code: 404
Error Caching Minimum TTL: 0
Response Page Path: /index.html
HTTP Response Code: 200
```

#### 5.2 Update Route 53

Change frontend CNAME to CloudFront distribution:
```
Type: A (Alias)
Name: www.yourdomain.com
Value: CloudFront Distribution Domain
```

---

## Configuration

### Environment Variables

**Elastic Beanstalk Environment Variables:**

```bash
DB_PASSWORD=<RDS_PASSWORD>
JWT_SECRET=<SECURE_SECRET_KEY>
SPRING_PROFILES_ACTIVE=prod
```

Set via EB CLI:
```bash
eb setenv KEY=VALUE
```

Or via AWS Console:
- Elastic Beanstalk → Environment → Configuration → Software

### Application Properties

Production `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://${RDS_ENDPOINT}:3306/inventory_management_db
spring.datasource.username=${DB_USERNAME:admin}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Server
server.port=5000
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain

# JWT
jwt.secret=${JWT_SECRET}
jwt.access-token.expiration=900000
jwt.refresh-token.expiration=604800000

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized

# Logging
logging.level.root=WARN
logging.level.com.ims.stockmanagement=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

---

## Security Best Practices

### 1. Database Security

- **Private Subnet:** Place RDS in private subnet (no public access)
- **Security Groups:** Restrict to Elastic Beanstalk security group only
- **SSL/TLS:** Enable encrypted connections
- **Automated Backups:** 7-day retention minimum
- **Master Password:** Use AWS Secrets Manager

### 2. Application Security

- **HTTPS Only:** Use SSL certificates for all traffic
- **Security Groups:** Minimum required ports open
- **IAM Roles:** Use instance roles instead of credentials
- **Secrets:** Store in AWS Secrets Manager or Parameter Store
- **CORS:** Restrict to known origins

### 3. Network Security

- **VPC:** Use dedicated VPC for production
- **Subnets:** Public subnets for ALB, private for instances
- **NACLs:** Network Access Control Lists for additional security
- **WAF:** AWS Web Application Firewall for advanced protection

### 4. Monitoring & Alerts

- **CloudWatch Alarms:**
  - CPU Utilization > 80%
  - Database connections > threshold
  - Error rate > 5%
  - Disk space < 20%

- **AWS GuardDuty:** Threat detection
- **AWS Config:** Resource compliance monitoring

---

## Monitoring & Maintenance

### CloudWatch Dashboards

Create custom dashboard with:
- Elastic Beanstalk health metrics
- RDS performance metrics
- Application error rates
- Request counts
- Response times

### Log Management

**Access Logs:**
```bash
# Stream logs in real-time
eb logs --stream

# Download logs
eb logs --all
```

**CloudWatch Logs:**
- Automatic log aggregation
- Log retention: 30 days
- Log insights for analysis

### Backup Strategy

**RDS Automated Backups:**
- Daily automatic snapshots
- 7-day retention
- Manual snapshots before major changes

**Application Backups:**
```bash
# Create EB environment snapshot
eb create stock-management-backup-$(date +%Y%m%d)

# Backup S3 frontend
aws s3 sync s3://stock-management-frontend-prod s3://backups/frontend-$(date +%Y%m%d)
```

### Updates & Rollbacks

**Rolling Updates:**
```bash
# Deploy new version
eb deploy

# Rollback if needed
eb deploy --version previous-version
```

**Blue/Green Deployment:**
```bash
# Clone environment
eb clone stock-management-prod --clone_name stock-management-staging

# Test staging
# Swap URLs when ready
eb swap stock-management-prod --destination_name stock-management-staging
```

---

## Cost Estimation

### Free Tier (First 12 Months)

**Estimated Monthly Cost:** $0 - $10

- **RDS (db.t3.micro):** 750 hours free
- **EC2 (t3.micro):** 750 hours free
- **S3:** 5 GB storage free
- **Data Transfer:** 15 GB out free
- **CloudFront:** 50 GB + 2M requests free

### Production Tier

**Estimated Monthly Cost:** $50 - $100

- **RDS (db.t3.small):** ~$30
- **Elastic Beanstalk (t3.small x 2):** ~$30
- **S3 + CloudFront:** ~$5-10
- **Load Balancer:** ~$20
- **Data Transfer:** ~$5-10

**Cost Optimization Tips:**
- Use Reserved Instances for predictable workloads
- Enable auto-scaling to scale down during off-hours
- Use S3 lifecycle policies for old logs
- Monitor unused resources

---

## Troubleshooting

### Common Issues

#### 1. Database Connection Failures

**Symptoms:** Application cannot connect to RDS

**Solutions:**
```bash
# Check security group rules
aws ec2 describe-security-groups --group-ids sg-xxxxxxxx

# Verify RDS status
aws rds describe-db-instances --db-instance-identifier inventory-management-db

# Test connection from EB instance
eb ssh
mysql -h <RDS_ENDPOINT> -u admin -p
```

#### 2. Application Not Starting

**Symptoms:** Environment health is degraded

**Solutions:**
```bash
# Check logs
eb logs --all

# Verify environment variables
eb printenv

# Check Java version
eb ssh
java --version
```

#### 3. Frontend Not Loading

**Symptoms:** S3 website returns errors

**Solutions:**
```bash
# Check bucket policy
aws s3api get-bucket-policy --bucket stock-management-frontend-prod

# Verify files uploaded
aws s3 ls s3://stock-management-frontend-prod

# Test CORS
curl -H "Origin: http://example.com" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     http://your-bucket.s3-website-region.amazonaws.com
```

#### 4. High Latency

**Symptoms:** Slow response times

**Solutions:**
- Enable CloudFront CDN
- Increase instance size
- Enable database caching
- Optimize database queries
- Add database read replicas

---

## Deployment Checklist

### Pre-Deployment

- [ ] AWS account configured
- [ ] IAM permissions verified
- [ ] Application tested locally
- [ ] Environment variables prepared
- [ ] SSL certificates obtained (if using HTTPS)

### Deployment Steps

- [ ] RDS instance created and configured
- [ ] Elastic Beanstalk environment initialized
- [ ] Application deployed to EB
- [ ] Health checks passing
- [ ] S3 bucket created for frontend
- [ ] Frontend built and uploaded
- [ ] DNS configured (if using custom domain)
- [ ] CloudFront distribution created (optional)

### Post-Deployment

- [ ] All endpoints tested
- [ ] Admin user created in database
- [ ] CloudWatch alarms configured
- [ ] Backup strategy implemented
- [ ] Documentation updated
- [ ] Team trained on monitoring

---

## Additional Resources

### AWS Documentation

- [Elastic Beanstalk Java Platform](https://docs.aws.amazon.com/elasticbeanstalk/latest/platforms/platforms-supported.html#platforms-supported.java)
- [RDS MySQL Documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_MySQL.html)
- [S3 Static Website Hosting](https://docs.aws.amazon.com/AmazonS3/latest/userguide/WebsiteHosting.html)
- [CloudFront Documentation](https://docs.aws.amazon.com/cloudfront/)

### Useful Commands

```bash
# EB Commands
eb init
eb create
eb deploy
eb logs
eb ssh
eb terminate

# AWS CLI Commands
aws rds describe-db-instances
aws s3 sync
aws elasticbeanstalk describe-environments
aws cloudfront create-invalidation
```

---

## Support & Maintenance

For production support:
- Monitor CloudWatch dashboards daily
- Review logs weekly
- Apply security patches monthly
- Perform load testing quarterly
- Review and optimize costs quarterly

---

**Document Version:** 1.0  
**Last Updated:** December 2024  
**Author:** Mehmet Taha Boynikoğlu

---

**End of AWS Deployment Guide**
