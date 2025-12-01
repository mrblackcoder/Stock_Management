#!/bin/bash

##############################################################################
# AWS Deployment Script for Stock Management System
# Author: Mehmet Taha Boynikoğlu
# Description: Automated deployment to AWS Elastic Beanstalk
##############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="inventory-management-api"
ENV_NAME="inventory-api-prod"
REGION="us-east-1"
PLATFORM="Corretto 21 running on 64bit Amazon Linux 2023"

echo -e "${GREEN}==================================================================${NC}"
echo -e "${GREEN}  AWS Deployment Script - Stock Management System${NC}"
echo -e "${GREEN}==================================================================${NC}"

# Check AWS CLI
if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI is not installed${NC}"
    echo "Install: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"
    exit 1
fi

# Check EB CLI
if ! command -v eb &> /dev/null; then
    echo -e "${RED}Error: Elastic Beanstalk CLI is not installed${NC}"
    echo "Install: pip install awsebcli"
    exit 1
fi

# Check if AWS credentials are configured
if ! aws sts get-caller-identity &> /dev/null; then
    echo -e "${RED}Error: AWS credentials not configured${NC}"
    echo "Run: aws configure"
    exit 1
fi

echo -e "${GREEN}✓ Prerequisites check passed${NC}\n"

# Step 1: Build JAR
echo -e "${YELLOW}Step 1: Building JAR file...${NC}"
cd ../..
./gradlew clean build -x test

if [ ! -f "build/libs/StockManagement-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${RED}Error: JAR file not found${NC}"
    exit 1
fi

echo -e "${GREEN}✓ JAR built successfully${NC}\n"

# Step 2: Create application.properties for production
echo -e "${YELLOW}Step 2: Creating production configuration...${NC}"
cat > src/main/resources/application-production.properties << 'PROPS'
# Production Configuration
spring.profiles.active=production

# Server Configuration
server.port=5000

# Database (RDS)
spring.datasource.url=${RDS_JDBC_URL}
spring.datasource.username=${RDS_USERNAME}
spring.datasource.password=${RDS_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# CORS
cors.allowed-origins=${CORS_ALLOWED_ORIGINS}

# Logging
logging.level.root=INFO
logging.level.com.ims.stockmanagement=INFO
PROPS

echo -e "${GREEN}✓ Production config created${NC}\n"

# Step 3: Initialize EB (if not done)
echo -e "${YELLOW}Step 3: Initializing Elastic Beanstalk...${NC}"
if [ ! -d ".elasticbeanstalk" ]; then
    eb init -p "$PLATFORM" $APP_NAME --region $REGION
    echo -e "${GREEN}✓ EB initialized${NC}\n"
else
    echo -e "${GREEN}✓ EB already initialized${NC}\n"
fi

# Step 4: Create environment (if not exists)
echo -e "${YELLOW}Step 4: Checking EB environment...${NC}"
if ! eb list | grep -q "$ENV_NAME"; then
    echo "Creating new environment: $ENV_NAME"
    
    eb create $ENV_NAME \
        --instance-type t2.small \
        --region $REGION \
        --envvars \
            SPRING_PROFILES_ACTIVE=production \
            JWT_SECRET="your-production-secret-key-min-256-bits-change-this" \
            JWT_EXPIRATION="86400000" \
            CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://www.yourdomain.com"
    
    echo -e "${GREEN}✓ Environment created${NC}\n"
else
    echo -e "${GREEN}✓ Environment already exists${NC}\n"
fi

# Step 5: Set environment variables
echo -e "${YELLOW}Step 5: Setting environment variables...${NC}"
read -sp "Enter RDS Database URL: " RDS_URL
echo
read -sp "Enter RDS Username: " RDS_USER
echo
read -sp "Enter RDS Password: " RDS_PASS
echo
read -sp "Enter JWT Secret (min 256 bits): " JWT_SEC
echo

eb setenv \
    RDS_JDBC_URL="$RDS_URL" \
    RDS_USERNAME="$RDS_USER" \
    RDS_PASSWORD="$RDS_PASS" \
    JWT_SECRET="$JWT_SEC" \
    SPRING_PROFILES_ACTIVE="production" \
    CORS_ALLOWED_ORIGINS="https://yourdomain.com"

echo -e "${GREEN}✓ Environment variables set${NC}\n"

# Step 6: Deploy
echo -e "${YELLOW}Step 6: Deploying to AWS...${NC}"
eb deploy $ENV_NAME

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Deployment successful${NC}\n"
    
    # Get URL
    URL=$(eb status $ENV_NAME | grep "CNAME" | awk '{print $2}')
    echo -e "${GREEN}==================================================================${NC}"
    echo -e "${GREEN}  Deployment Complete!${NC}"
    echo -e "${GREEN}==================================================================${NC}"
    echo -e "Application URL: ${YELLOW}http://$URL${NC}"
    echo -e "Health Check: ${YELLOW}http://$URL/actuator/health${NC}"
    echo -e "\nNext steps:"
    echo -e "1. Configure custom domain in Route 53"
    echo -e "2. Add SSL certificate in ACM"
    echo -e "3. Configure HTTPS listener in load balancer"
    echo -e "4. Update frontend API endpoint"
    echo -e "${GREEN}==================================================================${NC}"
else
    echo -e "${RED}Deployment failed${NC}"
    echo "Check logs: eb logs"
    exit 1
fi
