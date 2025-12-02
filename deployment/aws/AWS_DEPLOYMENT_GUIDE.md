# AWS Deployment Guide - Stock Management System

## Overview

This guide provides step-by-step instructions for deploying the Stock Management System to AWS.

**Architecture:**
- **Database:** Amazon RDS (MySQL)
- **Backend:** AWS Elastic Beanstalk (Java/Spring Boot)
- **Frontend:** Amazon S3 + CloudFront (React)
- **SSL/TLS:** AWS Certificate Manager (ACM)
- **DNS:** Amazon Route 53

---

## Prerequisites

1. **AWS Account** with billing enabled
2. **AWS CLI** installed and configured
3. **EB CLI** installed (`pip install awsebcli`)
4. **Domain name** (optional, for custom domain)
5. **Gradle** and **Java 21** installed locally

---

## Step 1: Database Setup (RDS)

### 1.1 Create RDS MySQL Instance

```bash
aws rds create-db-instance \
    --db-instance-identifier inventory-db \
    --db-instance-class db.t3.micro \
    --engine mysql \
    --engine-version 8.0.35 \
    --master-username admin \
    --master-user-password YourSecurePassword123! \
    --allocated-storage 20 \
    --db-name inventory_management_db \
    --backup-retention-period 7 \
    --publicly-accessible \
    --region us-east-1
```

### 1.2 Configure Security Group

```bash
# Get default VPC security group
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=is-default,Values=true" --query "Vpcs[0].VpcId" --output text)
SG_ID=$(aws ec2 describe-security-groups --filters "Name=vpc-id,Values=$VPC_ID" "Name=group-name,Values=default" --query "SecurityGroups[0].GroupId" --output text)

# Allow MySQL connections
aws ec2 authorize-security-group-ingress \
    --group-id $SG_ID \
    --protocol tcp \
    --port 3306 \
    --cidr 0.0.0.0/0
```

### 1.3 Wait for RDS Instance

```bash
aws rds wait db-instance-available --db-instance-identifier inventory-db

# Get endpoint
RDS_ENDPOINT=$(aws rds describe-db-instances \
    --db-instance-identifier inventory-db \
    --query "DBInstances[0].Endpoint.Address" \
    --output text)

echo "RDS Endpoint: $RDS_ENDPOINT"
```

### 1.4 Test Connection

```bash
mysql -h $RDS_ENDPOINT -u admin -p
# Enter password: YourSecurePassword123!

# Test database
SHOW DATABASES;
USE inventory_management_db;
```

---

## Step 2: Backend Deployment (Elastic Beanstalk)

### 2.1 Build Application

```bash
cd /path/to/Stock_Management

# Build JAR
./gradlew clean build -x test

# Verify JAR exists
ls -lh build/libs/StockManagement-0.0.1-SNAPSHOT.jar
```

### 2.2 Initialize Elastic Beanstalk

```bash
eb init -p "Corretto 21 running on 64bit Amazon Linux 2023" \
    inventory-management-api \
    --region us-east-1
```

### 2.3 Create Environment

```bash
eb create inventory-api-prod \
    --instance-type t2.small \
    --database.engine mysql \
    --database.instance $RDS_ENDPOINT
```

### 2.4 Set Environment Variables

```bash
eb setenv \
    SPRING_PROFILES_ACTIVE=production \
    SPRING_DATASOURCE_URL="jdbc:mysql://$RDS_ENDPOINT:3306/inventory_management_db" \
    SPRING_DATASOURCE_USERNAME=admin \
    SPRING_DATASOURCE_PASSWORD=YourSecurePassword123! \
    JWT_SECRET="your-super-secret-jwt-key-minimum-256-bits-long-change-this-in-production" \
    JWT_EXPIRATION=86400000 \
    CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com"
```

### 2.5 Deploy Application

```bash
eb deploy inventory-api-prod

# Check status
eb status inventory-api-prod

# Get URL
eb open inventory-api-prod
```

### 2.6 Test Deployment

```bash
# Get EB URL
EB_URL=$(eb status inventory-api-prod | grep "CNAME" | awk '{print $2}')

# Test health endpoint
curl http://$EB_URL/actuator/health

# Test login endpoint
curl -X POST http://$EB_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## Step 3: Frontend Deployment (S3 + CloudFront)

### 3.1 Build React App

```bash
cd frontend

# Update API endpoint
cat > .env.production << ENV
REACT_APP_API_URL=https://api.yourdomain.com/api
ENV

# Build
npm install
npm run build
```

### 3.2 Create S3 Bucket

```bash
BUCKET_NAME="inventory-management-frontend"

# Create bucket
aws s3 mb s3://$BUCKET_NAME --region us-east-1

# Configure static website hosting
aws s3 website s3://$BUCKET_NAME \
    --index-document index.html \
    --error-document index.html

# Upload files
aws s3 sync build/ s3://$BUCKET_NAME/ \
    --delete \
    --cache-control "public, max-age=31536000"

# Upload index.html with no-cache
aws s3 cp build/index.html s3://$BUCKET_NAME/index.html \
    --cache-control "no-cache"
```

### 3.3 Configure Bucket Policy

```bash
cat > bucket-policy.json << POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::$BUCKET_NAME/*"
    }
  ]
}
POLICY

aws s3api put-bucket-policy \
    --bucket $BUCKET_NAME \
    --policy file://bucket-policy.json
```

### 3.4 Create CloudFront Distribution

```bash
aws cloudfront create-distribution \
    --origin-domain-name $BUCKET_NAME.s3-website-us-east-1.amazonaws.com \
    --default-root-object index.html \
    --comment "Inventory Management Frontend"

# Get distribution ID
DIST_ID=$(aws cloudfront list-distributions \
    --query "DistributionList.Items[?Comment=='Inventory Management Frontend'].Id" \
    --output text)

echo "CloudFront Distribution ID: $DIST_ID"
```

---

## Step 4: SSL Certificate (ACM)

### 4.1 Request Certificate

```bash
aws acm request-certificate \
    --domain-name yourdomain.com \
    --subject-alternative-names www.yourdomain.com api.yourdomain.com \
    --validation-method DNS \
    --region us-east-1

# Get certificate ARN
CERT_ARN=$(aws acm list-certificates \
    --query "CertificateSummaryList[?DomainName=='yourdomain.com'].CertificateArn" \
    --output text)

echo "Certificate ARN: $CERT_ARN"
```

### 4.2 Validate Domain

```bash
# Get validation records
aws acm describe-certificate \
    --certificate-arn $CERT_ARN \
    --query "Certificate.DomainValidationOptions"

# Add CNAME records to your DNS provider (Route 53, GoDaddy, etc.)
```

### 4.3 Wait for Validation

```bash
aws acm wait certificate-validated --certificate-arn $CERT_ARN
echo "Certificate validated!"
```

---

## Step 5: Configure HTTPS

### 5.1 Update Elastic Beanstalk

```bash
# Add HTTPS listener
eb console inventory-api-prod
# Go to Configuration → Load Balancer → Add Listener
# Protocol: HTTPS, Port: 443, SSL Certificate: [Select your certificate]
```

### 5.2 Update CloudFront

```bash
# Update distribution to use HTTPS
aws cloudfront update-distribution \
    --id $DIST_ID \
    --viewer-certificate ACMCertificateArn=$CERT_ARN,SSLSupportMethod=sni-only
```

---

## Step 6: DNS Configuration (Route 53)

### 6.1 Create Hosted Zone

```bash
aws route53 create-hosted-zone \
    --name yourdomain.com \
    --caller-reference $(date +%s)

# Get hosted zone ID
ZONE_ID=$(aws route53 list-hosted-zones \
    --query "HostedZones[?Name=='yourdomain.com.'].Id" \
    --output text | cut -d'/' -f3)
```

### 6.2 Create DNS Records

```bash
# Get CloudFront domain
CF_DOMAIN=$(aws cloudfront get-distribution --id $DIST_ID \
    --query "Distribution.DomainName" --output text)

# Create A record for frontend
cat > change-batch-frontend.json << JSON
{
  "Changes": [{
    "Action": "CREATE",
    "ResourceRecordSet": {
      "Name": "yourdomain.com",
      "Type": "A",
      "AliasTarget": {
        "HostedZoneId": "Z2FDTNDATAQYW2",
        "DNSName": "$CF_DOMAIN",
        "EvaluateTargetHealth": false
      }
    }
  }]
}
JSON

aws route53 change-resource-record-sets \
    --hosted-zone-id $ZONE_ID \
    --change-batch file://change-batch-frontend.json

# Create CNAME for API
cat > change-batch-api.json << JSON
{
  "Changes": [{
    "Action": "CREATE",
    "ResourceRecordSet": {
      "Name": "api.yourdomain.com",
      "Type": "CNAME",
      "TTL": 300,
      "ResourceRecords": [{"Value": "$EB_URL"}]
    }
  }]
}
JSON

aws route53 change-resource-record-sets \
    --hosted-zone-id $ZONE_ID \
    --change-batch file://change-batch-api.json
```

---

## Step 7: Testing

### 7.1 Test Backend

```bash
# Test HTTPS
curl https://api.yourdomain.com/actuator/health

# Test API
curl -X POST https://api.yourdomain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 7.2 Test Frontend

```bash
# Open in browser
open https://yourdomain.com

# Check CloudFront
curl -I https://yourdomain.com
```

---

## Step 8: Monitoring & Logging

### 8.1 Enable CloudWatch Logs

```bash
eb logs --cloudwatch-logs enable inventory-api-prod
```

### 8.2 View Logs

```bash
# View recent logs
eb logs inventory-api-prod

# Stream logs
aws logs tail /aws/elasticbeanstalk/inventory-api-prod/var/log/eb-engine.log --follow
```

### 8.3 Set Up Alarms

```bash
# CPU utilization alarm
aws cloudwatch put-metric-alarm \
    --alarm-name inventory-api-high-cpu \
    --alarm-description "Alert when CPU exceeds 80%" \
    --metric-name CPUUtilization \
    --namespace AWS/EC2 \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2
```

---

## Cost Optimization

### Free Tier Eligible (12 months)
- **RDS:** 750 hours/month db.t3.micro
- **EC2:** 750 hours/month t2.micro
- **S3:** 5 GB storage, 20,000 GET requests
- **CloudFront:** 1 TB transfer

### Estimated Monthly Cost (After Free Tier)
| Service | Configuration | Cost |
|---------|--------------|------|
| RDS MySQL | db.t3.micro, 20GB | $15 |
| Elastic Beanstalk | t2.small | $17 |
| S3 | 5GB | $0.12 |
| CloudFront | 1GB transfer | $0.085 |
| Route 53 | 1 hosted zone | $0.50 |
| **Total** | | **~$33/month** |

---

## Troubleshooting

### Issue: EB deployment fails

```bash
# Check logs
eb logs inventory-api-prod

# SSH to instance
eb ssh inventory-api-prod

# Check application logs
sudo tail -f /var/log/eb-engine.log
```

### Issue: Database connection timeout

```bash
# Check security group
aws ec2 describe-security-groups --group-ids $SG_ID

# Test from EC2
eb ssh inventory-api-prod
mysql -h $RDS_ENDPOINT -u admin -p
```

### Issue: CloudFront not updating

```bash
# Invalidate cache
aws cloudfront create-invalidation \
    --distribution-id $DIST_ID \
    --paths "/*"
```

---

## Maintenance

### Update Application

```bash
# Build new version
./gradlew clean build -x test

# Deploy
eb deploy inventory-api-prod
```

### Database Backup

```bash
# Manual snapshot
aws rds create-db-snapshot \
    --db-instance-identifier inventory-db \
    --db-snapshot-identifier inventory-db-snapshot-$(date +%Y%m%d)
```

### Scale Application

```bash
# Scale up
eb scale 2 inventory-api-prod

# Configure auto-scaling
eb console inventory-api-prod
# Go to Configuration → Capacity → Edit
```

---

## Cleanup (Delete All Resources)

```bash
# Delete EB environment
eb terminate inventory-api-prod --force

# Delete RDS instance
aws rds delete-db-instance \
    --db-instance-identifier inventory-db \
    --skip-final-snapshot

# Delete S3 bucket
aws s3 rb s3://$BUCKET_NAME --force

# Delete CloudFront distribution
aws cloudfront delete-distribution \
    --id $DIST_ID \
    --if-match $(aws cloudfront get-distribution --id $DIST_ID --query "Distribution.ETag" --output text)

# Delete Route 53 hosted zone
aws route53 delete-hosted-zone --id $ZONE_ID
```

---

**Last Updated:** December 2024  
**Maintained by:** Mehmet Taha Boynikoğlu  
**Support:** Email or GitHub Issues
