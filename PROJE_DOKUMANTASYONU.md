# Stock Management System - Proje Dokümantasyonu

**Öğrenci:** Mehmet Taha Boynikoğlu
**Öğrenci No:** 212 125 10 34
**Ders:** Web Design and Programming
**Üniversite:** Fatih Sultan Mehmet Vakıf Üniversitesi
**Tarih:** Aralık 2024

---

## İçindekiler

1. [Proje Özeti](#1-proje-özeti)
2. [Teknoloji Stack](#2-teknoloji-stack)
3. [Proje Mimarisi](#3-proje-mimarisi)
4. [Veritabanı Yapısı](#4-veritabanı-yapısı)
5. [Güvenlik Özellikleri](#5-güvenlik-özellikleri)
6. [API Endpoints](#6-api-endpoints)
7. [Tamamlanan İşler](#7-tamamlanan-işler)
8. [Yerel Kurulum](#8-yerel-kurulum)
9. [Docker ile Çalıştırma](#9-docker-ile-çalıştırma)
10. [AWS Deployment](#10-aws-deployment)
11. [CI/CD Pipeline](#11-cicd-pipeline)
12. [Sonraki Adımlar](#12-sonraki-adımlar)

---

## 1. Proje Özeti

Stock Management System (Envanter Yönetim Sistemi), kurumsal düzeyde bir stok takip ve yönetim uygulamasıdır. Sistem şu temel özellikleri sunar:

- **Ürün Yönetimi:** Ürün ekleme, düzenleme, silme ve listeleme
- **Kategori Yönetimi:** Ürünleri kategorilere ayırma
- **Tedarikçi Yönetimi:** Tedarikçi bilgilerini takip
- **Stok İşlemleri:** Alım, satım ve stok ayarlama işlemleri
- **Düşük Stok Uyarıları:** Kritik stok seviyesine düşen ürünlerin tespiti
- **Kullanıcı Yönetimi:** Kayıt, giriş ve rol tabanlı yetkilendirme
- **Döviz Kuru Entegrasyonu:** Harici API ile güncel döviz kurları

### Proje Hedefleri

1. Modern web teknolojileri kullanarak tam yığın (full-stack) uygulama geliştirme
2. RESTful API tasarım prensiplerine uygun backend geliştirme
3. JWT tabanlı güvenli kimlik doğrulama sistemi kurma
4. Katmanlı mimari (Layered Architecture) uygulama
5. Docker ve AWS ile deployment yapabilme

---

## 2. Teknoloji Stack

### Backend
| Teknoloji | Versiyon | Kullanım Amacı |
|-----------|----------|----------------|
| Java | 21 | Ana programlama dili |
| Spring Boot | 3.5.7 | Web framework |
| Spring Security | 6.x | Güvenlik ve yetkilendirme |
| Spring Data JPA | 3.x | Veritabanı işlemleri |
| MySQL | 8.0 | İlişkisel veritabanı |
| JWT | - | Token tabanlı kimlik doğrulama |
| Gradle | 8.5 | Build aracı |
| Swagger/OpenAPI | 3.x | API dokümantasyonu |

### Frontend
| Teknoloji | Versiyon | Kullanım Amacı |
|-----------|----------|----------------|
| React | 19.2.0 | UI framework |
| Axios | 1.x | HTTP istemcisi |
| Bootstrap | 5.x | CSS framework |
| CryptoJS | 4.x | Token şifreleme |
| React Router | 7.x | Sayfa yönlendirme |

### DevOps & Deployment
| Teknoloji | Kullanım Amacı |
|-----------|----------------|
| Docker | Konteynerizasyon |
| Docker Compose | Çoklu konteyner yönetimi |
| GitHub Actions | CI/CD pipeline |
| AWS ECS | Konteyner orkestrasyon |
| AWS S3 | Statik dosya barındırma |
| AWS CloudFront | CDN |
| AWS RDS | Yönetilen MySQL |
| Terraform | Infrastructure as Code |

---

## 3. Proje Mimarisi

### Katmanlı Mimari (Layered Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (React)                        │
│   LoginPage │ Dashboard │ Products │ Transactions │ etc.    │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP/REST
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  AuthController │ ProductController │ TransactionController  │
│                                                              │
│  - HTTP isteklerini alır                                    │
│  - DTO validasyonu yapar                                    │
│  - Response döner                                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│   AuthService │ ProductService │ StockTransactionService     │
│                                                              │
│  - İş mantığını (business logic) içerir                     │
│  - @Transactional ile transaction yönetimi                  │
│  - @PreAuthorize ile method-level güvenlik                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Repository Layer                           │
│  UserRepository │ ProductRepository │ TransactionRepository  │
│                                                              │
│  - JPA/Hibernate ile veritabanı erişimi                     │
│  - JPQL sorgular                                            │
│  - N+1 optimizasyonu (FETCH JOIN)                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Entity Layer                             │
│     User │ Product │ Category │ Supplier │ Transaction       │
│                                                              │
│  - JPA entity sınıfları                                     │
│  - Veritabanı tablo mappingleri                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    MySQL Database                            │
│                                                              │
│  users │ products │ categories │ suppliers │ transactions   │
└─────────────────────────────────────────────────────────────┘
```

### Dosya Yapısı

```
Stock_Management/
├── src/main/java/com/ims/stockmanagement/
│   ├── StockManagementApplication.java    # Ana uygulama
│   ├── config/                            # Konfigürasyon
│   │   └── AdminInitializer.java
│   ├── controllers/                       # REST Controller'lar
│   │   ├── AuthController.java
│   │   ├── ProductController.java
│   │   ├── CategoryController.java
│   │   ├── SupplierController.java
│   │   ├── StockTransactionController.java
│   │   └── UserController.java
│   ├── dtos/                              # Data Transfer Objects
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   └── ProductRequest.java
│   │   └── response/
│   │       ├── ApiResponse.java
│   │       ├── UserDTO.java
│   │       ├── ProductDTO.java
│   │       └── TransactionDTO.java
│   ├── enums/                             # Enum sınıfları
│   │   ├── Role.java
│   │   └── TransactionType.java
│   ├── models/                            # JPA Entity'ler
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Category.java
│   │   ├── Supplier.java
│   │   ├── StockTransaction.java
│   │   └── RefreshToken.java
│   ├── repositories/                      # JPA Repository'ler
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   └── ...
│   ├── security/                          # Güvenlik
│   │   ├── SecurityConfig.java
│   │   ├── JwtService.java
│   │   └── JwtAuthenticationFilter.java
│   └── strategy/                          # Strategy Pattern
│       └── LoginAttemptService.java
├── src/main/resources/
│   ├── application.properties             # Konfigürasyon
│   └── templates/                         # Thymeleaf şablonları
├── frontend/                              # React uygulaması
│   ├── src/
│   │   ├── pages/                         # Sayfa bileşenleri
│   │   ├── service/                       # API servisleri
│   │   └── App.js
│   └── package.json
├── .github/workflows/                     # CI/CD
│   ├── ci.yml
│   └── deploy-aws.yml
├── docker-compose.yml                     # Docker yapılandırması
├── Dockerfile                             # Backend Docker image
└── terraform/                             # AWS altyapı kodu
```

---

## 4. Veritabanı Yapısı

### ER Diyagramı

```
┌──────────────────┐       ┌──────────────────┐
│      users       │       │   categories     │
├──────────────────┤       ├──────────────────┤
│ id (PK)          │       │ id (PK)          │
│ username         │       │ name             │
│ email            │       │ description      │
│ password_hash    │       │ created_at       │
│ role             │       │ updated_at       │
│ created_at       │       └────────┬─────────┘
│ updated_at       │                │
└────────┬─────────┘                │
         │                          │
         │                          │
         │                          │ 1:N
         │                          │
         │    ┌──────────────────┐  │
         │    │    products      │◄─┘
         │    ├──────────────────┤
         │    │ id (PK)          │
         │    │ name             │
         │    │ sku              │
         │    │ description      │
         │    │ price            │
         │    │ quantity         │
         │    │ min_quantity     │
         │    │ category_id (FK) │
         │    │ supplier_id (FK) │◄───────────┐
         │    │ created_at       │            │
         │    │ updated_at       │            │
         │    └────────┬─────────┘            │
         │             │                      │
         │             │ 1:N            ┌─────┴──────────┐
         │             │                │   suppliers    │
         │             ▼                ├────────────────┤
┌────────┴─────────────────────┐       │ id (PK)        │
│    stock_transactions        │       │ name           │
├──────────────────────────────┤       │ contact_person │
│ id (PK)                      │       │ email          │
│ product_id (FK)              │       │ phone          │
│ user_id (FK)                 │       │ address        │
│ transaction_type             │       │ created_at     │
│ quantity                     │       │ updated_at     │
│ notes                        │       └────────────────┘
│ created_at                   │
└──────────────────────────────┘

┌──────────────────┐
│  refresh_tokens  │
├──────────────────┤
│ id (PK)          │
│ token            │
│ user_id (FK)     │
│ expiry_date      │
│ revoked          │
└──────────────────┘
```

### Tablolar ve Alanlar

#### users Tablosu
| Alan | Tip | Açıklama |
|------|-----|----------|
| id | BIGINT | Primary Key |
| username | VARCHAR(50) | Kullanıcı adı (unique) |
| email | VARCHAR(100) | E-posta (unique) |
| password | VARCHAR(255) | BCrypt hash |
| role | ENUM | USER veya ADMIN |
| created_at | TIMESTAMP | Oluşturulma tarihi |
| updated_at | TIMESTAMP | Güncellenme tarihi |

#### products Tablosu
| Alan | Tip | Açıklama |
|------|-----|----------|
| id | BIGINT | Primary Key |
| name | VARCHAR(100) | Ürün adı |
| sku | VARCHAR(50) | Stok kodu (unique) |
| description | TEXT | Açıklama |
| price | DECIMAL(10,2) | Birim fiyat |
| quantity | INT | Mevcut stok |
| min_quantity | INT | Minimum stok seviyesi |
| category_id | BIGINT | Foreign Key → categories |
| supplier_id | BIGINT | Foreign Key → suppliers |

#### stock_transactions Tablosu
| Alan | Tip | Açıklama |
|------|-----|----------|
| id | BIGINT | Primary Key |
| product_id | BIGINT | Foreign Key → products |
| user_id | BIGINT | Foreign Key → users |
| transaction_type | ENUM | PURCHASE, SALE, ADJUSTMENT |
| quantity | INT | İşlem miktarı |
| notes | TEXT | Notlar |
| created_at | TIMESTAMP | İşlem tarihi |

---

## 5. Güvenlik Özellikleri

### JWT Authentication Akışı

```
┌─────────┐                    ┌─────────┐                    ┌─────────┐
│ Client  │                    │ Backend │                    │  MySQL  │
└────┬────┘                    └────┬────┘                    └────┬────┘
     │                              │                              │
     │  1. POST /api/auth/login     │                              │
     │  {username, password}        │                              │
     │─────────────────────────────►│                              │
     │                              │  2. Find user by username    │
     │                              │─────────────────────────────►│
     │                              │◄─────────────────────────────│
     │                              │  3. Verify password (BCrypt) │
     │                              │                              │
     │  4. Return tokens            │                              │
     │  {accessToken, refreshToken} │                              │
     │◄─────────────────────────────│                              │
     │                              │                              │
     │  5. Store tokens (encrypted) │                              │
     │  localStorage + CryptoJS     │                              │
     │                              │                              │
     │  6. GET /api/products        │                              │
     │  Authorization: Bearer token │                              │
     │─────────────────────────────►│                              │
     │                              │  7. Validate JWT             │
     │                              │  8. Check expiration         │
     │                              │  9. Extract user info        │
     │                              │                              │
     │  10. Return data             │                              │
     │◄─────────────────────────────│                              │
```

### Token Yapısı

**Access Token (15 dakika):**
```json
{
  "sub": "username",
  "role": "ADMIN",
  "iat": 1702400000,
  "exp": 1702400900
}
```

**Refresh Token (7 gün):**
- Veritabanında saklanır
- Access token yenilemek için kullanılır
- Her yenileme sonrası eski token geçersiz olur

### Frontend Token Güvenliği

```javascript
// Token şifreleme (CryptoJS AES)
const encryptToken = (token) => {
    return CryptoJS.AES.encrypt(token, SECRET_KEY).toString();
};

// Token çözme
const decryptToken = (encryptedToken) => {
    const bytes = CryptoJS.AES.decrypt(encryptedToken, SECRET_KEY);
    return bytes.toString(CryptoJS.enc.Utf8);
};
```

### Güvenlik Başlıkları (Security Headers)

```java
// SecurityConfig.java
headers.frameOptions(frame -> frame.deny());           // Clickjacking koruması
headers.contentTypeOptions(contentType -> {});         // MIME sniffing koruması
headers.xssProtection(xss -> xss.headerValue(...));   // XSS koruması
headers.contentSecurityPolicy(csp -> csp.policy(...)); // CSP
headers.referrerPolicy(referrer -> referrer.policy()); // Referrer policy
```

### Rol Hiyerarşisi

```java
// ADMIN tüm USER yetkilerine sahip
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
    return hierarchy;
}
```

### Brute Force Koruması

```java
// LoginAttemptService - Rate limiting
- Başarısız giriş denemeleri sayılır
- 5 başarısız denemeden sonra hesap kilitlenir
- 15 dakika sonra kilit açılır
```

---

## 6. API Endpoints

### Authentication API

| Method | Endpoint | Açıklama | Yetki |
|--------|----------|----------|-------|
| POST | `/api/auth/register` | Yeni kullanıcı kaydı | Public |
| POST | `/api/auth/login` | Giriş yap, token al | Public |
| POST | `/api/auth/refresh-token` | Token yenile | Public |
| POST | `/api/auth/logout` | Çıkış yap | Authenticated |

### Product API

| Method | Endpoint | Açıklama | Yetki |
|--------|----------|----------|-------|
| GET | `/api/products` | Tüm ürünleri listele | USER |
| GET | `/api/products/{id}` | Ürün detayı | USER |
| POST | `/api/products` | Yeni ürün ekle | USER |
| PUT | `/api/products/{id}` | Ürün güncelle | USER |
| DELETE | `/api/products/{id}` | Ürün sil | ADMIN |
| GET | `/api/products/low-stock` | Düşük stok uyarıları | USER |

### Category API

| Method | Endpoint | Açıklama | Yetki |
|--------|----------|----------|-------|
| GET | `/api/categories` | Tüm kategorileri listele | USER |
| GET | `/api/categories/{id}` | Kategori detayı | USER |
| POST | `/api/categories` | Yeni kategori ekle | ADMIN |
| PUT | `/api/categories/{id}` | Kategori güncelle | ADMIN |
| DELETE | `/api/categories/{id}` | Kategori sil | ADMIN |

### Supplier API

| Method | Endpoint | Açıklama | Yetki |
|--------|----------|----------|-------|
| GET | `/api/suppliers` | Tüm tedarikçileri listele | USER |
| GET | `/api/suppliers/{id}` | Tedarikçi detayı | USER |
| POST | `/api/suppliers` | Yeni tedarikçi ekle | ADMIN |
| PUT | `/api/suppliers/{id}` | Tedarikçi güncelle | ADMIN |
| DELETE | `/api/suppliers/{id}` | Tedarikçi sil | ADMIN |

### Transaction API

| Method | Endpoint | Açıklama | Yetki |
|--------|----------|----------|-------|
| GET | `/api/transactions` | Tüm işlemleri listele | USER |
| GET | `/api/transactions/{id}` | İşlem detayı | USER |
| POST | `/api/transactions` | Yeni stok işlemi | USER |
| GET | `/api/transactions/product/{id}` | Ürüne göre işlemler | USER |

### User API

| Method | Endpoint | Açıklama | Yetki |
|--------|----------|----------|-------|
| GET | `/api/users` | Tüm kullanıcıları listele | ADMIN |
| GET | `/api/users/{id}` | Kullanıcı detayı | ADMIN |
| PUT | `/api/users/{id}` | Kullanıcı güncelle | ADMIN |
| DELETE | `/api/users/{id}` | Kullanıcı sil | ADMIN |
| GET | `/api/users/profile` | Kendi profilini görüntüle | USER |

### API Response Format

```json
// Başarılı yanıt
{
  "status": 200,
  "message": "Product created successfully",
  "data": { ... }
}

// Hata yanıtı
{
  "status": 400,
  "message": "Validation failed",
  "errors": ["Name is required", "Price must be positive"]
}
```

---

## 7. Tamamlanan İşler

### Commit Geçmişi

| Commit | Açıklama |
|--------|----------|
| `4db83f1` | CI/CD test hatalarını düzeltme |
| `f6f555d` | TransactionPage.js alan adı düzeltmesi (userName → username) |
| `fc32f23` | Kapsamlı mimari düzeltmeleri ve kod kalitesi iyileştirmeleri |
| `239fb64` | API tutarlılığı, null güvenliği ve güvenlik temizliği |

### Yapılan Düzeltmeler

#### 1. Frontend Test Hatası
**Problem:** `App.test.js` varsayılan CRA testi "learn react" metni arıyordu
```javascript
// Eski (hatalı)
expect(screen.getByText(/learn react/i)).toBeInTheDocument();

// Yeni (düzeltilmiş)
const { container } = render(<App />);
expect(container).toBeInTheDocument();
```

#### 2. TransactionPage Alan Adı Uyumsuzluğu
**Problem:** Frontend `trans.userName` kullanıyordu, backend `username` dönüyordu
```javascript
// Eski (hatalı)
<td>{trans.userName || 'N/A'}</td>

// Yeni (düzeltilmiş)
<td>{trans.username || 'N/A'}</td>
```

#### 3. CI/CD MySQL Service Eksikliği
**Problem:** `deploy-aws.yml` test job'ında MySQL service container yoktu
**Çözüm:** MySQL service container ve environment variables eklendi

### Kod Kalitesi İyileştirmeleri

1. **N+1 Query Optimizasyonu:** FETCH JOIN ile ilişkili verilerin tek sorguda çekilmesi
2. **DTO Validasyonları:** @NotBlank, @Size, @Email, @Pattern annotation'ları
3. **Null Güvenliği:** Optional kullanımı ve null kontrolleri
4. **Exception Handling:** GlobalExceptionHandler ile merkezi hata yönetimi
5. **Transaction Yönetimi:** @Transactional ile atomik operasyonlar
6. **Security Headers:** XSS, CSP, HSTS, Clickjacking korumaları

---

## 8. Yerel Kurulum

### Gereksinimler

- Java 21 veya üstü
- Node.js 18 veya üstü
- MySQL 8.0 veya Docker
- Git

### Adım 1: Repository'yi Klonla

```bash
git clone https://github.com/mrblackcoder/Stock_Management.git
cd Stock_Management
```

### Adım 2: MySQL Başlat (Docker ile)

```bash
# MySQL container başlat
docker run --name stock-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=inventory_management_db \
  -p 3306:3306 \
  -d mysql:8.0

# 10 saniye bekle
sleep 10

# Kontrol et
docker ps | grep stock-mysql
```

### Adım 3: Backend Başlat

```bash
# Gradle ile çalıştır
./gradlew bootRun

# VEYA JAR oluştur ve çalıştır
./gradlew clean bootJar
java -jar build/libs/StockManagement-*.jar
```

Backend http://localhost:8080 adresinde çalışacak.

### Adım 4: Frontend Başlat

```bash
cd frontend
npm install
npm start
```

Frontend http://localhost:3000 adresinde çalışacak.

### Adım 5: Uygulamaya Eriş

| Adres | Açıklama |
|-------|----------|
| http://localhost:3000 | React Frontend |
| http://localhost:8080 | Backend API |
| http://localhost:8080/swagger-ui.html | API Dokümantasyonu |
| http://localhost:8080/actuator/health | Sağlık Kontrolü |

**Varsayılan Giriş Bilgileri:**
- Kullanıcı adı: `admin`
- Şifre: `Admin@123!Secure`

---

## 9. Docker ile Çalıştırma

### Tek Komut ile Tüm Stack

```bash
# Tüm servisleri başlat (MySQL + Backend + Frontend)
docker-compose up -d

# Logları izle
docker-compose logs -f

# Servisleri durdur
docker-compose down

# Servisleri ve verileri sil
docker-compose down -v
```

### Servis Durumlarını Kontrol Et

```bash
# Container durumları
docker ps

# Backend sağlık kontrolü
curl http://localhost:8080/actuator/health

# Frontend erişimi
curl http://localhost:80
```

### Docker Compose Yapısı

```yaml
services:
  mysql:
    image: mysql:8.0
    ports: ["3306:3306"]
    healthcheck: mysqladmin ping

  backend:
    build: .
    ports: ["8080:8080"]
    depends_on: mysql (healthy)

  frontend:
    build: ./frontend
    ports: ["80:80"]
    depends_on: backend (healthy)
```

---

## 10. AWS Deployment

### Mimari

```
┌─────────────────────────────────────────────────────────────────┐
│                           Internet                               │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                        CloudFront CDN                            │
│                    (HTTPS, Caching, WAF)                        │
└───────────┬───────────────────────────────────────┬─────────────┘
            │                                       │
            ▼                                       ▼
┌───────────────────────┐               ┌───────────────────────┐
│    S3 Bucket          │               │   Application Load    │
│  (React Static)       │               │      Balancer         │
└───────────────────────┘               └───────────┬───────────┘
                                                    │
                                                    ▼
                                        ┌───────────────────────┐
                                        │     ECS Fargate       │
                                        │   (Backend Java)      │
                                        └───────────┬───────────┘
                                                    │
                                                    ▼
                                        ┌───────────────────────┐
                                        │     RDS MySQL         │
                                        │  (Private Subnet)     │
                                        └───────────────────────┘
```

### GitHub Secrets Gereksinimleri

AWS deployment için aşağıdaki secrets'ları GitHub repository ayarlarına eklemeniz gerekir:

| Secret Adı | Açıklama | Nasıl Alınır |
|------------|----------|--------------|
| `AWS_ACCESS_KEY_ID` | AWS IAM Access Key | AWS Console → IAM → Users → Security credentials |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM Secret Key | Yukarıdaki adımla birlikte |
| `CLOUDFRONT_DISTRIBUTION_ID` | CloudFront ID | AWS Console → CloudFront → Distribution ID |
| `REACT_APP_API_URL` | Backend API URL | ALB DNS veya custom domain |
| `REACT_APP_ENCRYPTION_KEY` | Frontend şifreleme anahtarı | `openssl rand -base64 32` |

### GitHub Secrets Ekleme

1. GitHub repository'ye git
2. **Settings** → **Secrets and variables** → **Actions**
3. **New repository secret** butonuna tıkla
4. Her bir secret için ad ve değer gir

### AWS IAM Policy

IAM kullanıcısı için gerekli minimum izinler:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecs:UpdateService",
        "ecs:DescribeServices"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-frontend-bucket",
        "arn:aws:s3:::your-frontend-bucket/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "cloudfront:CreateInvalidation"
      ],
      "Resource": "*"
    }
  ]
}
```

### Manuel Deployment Adımları

Eğer CI/CD yerine manuel deployment tercih ederseniz:

#### 1. RDS MySQL Oluştur
```bash
# AWS Console → RDS → Create Database
# Engine: MySQL 8.0
# Template: Free Tier
# DB Instance: stock-management-db
# Endpoint not edin!
```

#### 2. EC2 Instance Oluştur ve Yapılandır
```bash
# SSH ile bağlan
ssh -i key.pem ec2-user@<EC2-IP>

# Java kur
sudo yum install -y java-21-amazon-corretto-devel

# Repository'yi klonla
git clone https://github.com/mrblackcoder/Stock_Management.git
cd Stock_Management

# Environment değişkenlerini ayarla
export SPRING_DATASOURCE_URL=jdbc:mysql://<RDS-ENDPOINT>:3306/inventory_management_db
export SPRING_DATASOURCE_PASSWORD=<RDS-PASSWORD>
export JWT_SECRET=$(openssl rand -base64 64)

# Build ve çalıştır
./gradlew clean bootJar
java -jar build/libs/StockManagement-*.jar
```

#### 3. S3 + CloudFront ile Frontend Deploy
```bash
cd frontend
npm install
npm run build

# S3'e yükle
aws s3 sync build/ s3://your-bucket-name/ --delete

# CloudFront cache temizle
aws cloudfront create-invalidation \
  --distribution-id YOUR_DIST_ID \
  --paths "/*"
```

---

## 11. CI/CD Pipeline

### Workflow Dosyaları

#### ci.yml (Her Push ve PR'da çalışır)
```yaml
name: CI
on: [push, pull_request]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: inventory_management_db
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
      - run: ./gradlew test

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '18'
      - run: cd frontend && npm ci && npm test
```

#### deploy-aws.yml (Main branch'e push'ta çalışır)
```yaml
name: Deploy to AWS
on:
  push:
    branches: [main]

jobs:
  test:
    # Testleri çalıştır

  deploy-backend:
    needs: test
    # ECR'a push et
    # ECS service güncelle

  deploy-frontend:
    needs: test
    # npm build
    # S3'e sync
    # CloudFront invalidate
```

### Pipeline Durumu

| Job | Durum | Açıklama |
|-----|-------|----------|
| Backend Tests | ✅ | MySQL service ile çalışıyor |
| Frontend Tests | ✅ | Smoke test başarılı |
| Deploy Backend | ⏳ | AWS secrets gerekli |
| Deploy Frontend | ⏳ | AWS secrets gerekli |

---

## 12. Sonraki Adımlar

### Yapılması Gerekenler (AWS Deployment için)

#### 1. AWS Hesabı Hazırlığı

- [ ] AWS hesabı oluştur veya mevcut hesaba giriş yap
- [ ] IAM kullanıcısı oluştur (programmatic access)
- [ ] Gerekli IAM policy'lerini ekle
- [ ] Access Key ID ve Secret Access Key'i not al

#### 2. AWS Altyapısını Kur

- [ ] RDS MySQL instance oluştur
- [ ] ECS Cluster oluştur
- [ ] ECR Repository oluştur
- [ ] S3 Bucket oluştur (frontend için)
- [ ] CloudFront Distribution oluştur
- [ ] Application Load Balancer konfigüre et

#### 3. GitHub Secrets Ekle

```
AWS_ACCESS_KEY_ID=<your-access-key>
AWS_SECRET_ACCESS_KEY=<your-secret-key>
CLOUDFRONT_DISTRIBUTION_ID=<your-distribution-id>
REACT_APP_API_URL=https://your-api-domain.com/api
REACT_APP_ENCRYPTION_KEY=<generate-with-openssl>
```

#### 4. Domain ve SSL (Opsiyonel)

- [ ] Custom domain satın al (örn: stockmanagement.com)
- [ ] Route 53'te hosted zone oluştur
- [ ] ACM ile SSL sertifikası al
- [ ] CloudFront ve ALB'ye SSL ekle

### Projeyi Geliştirmek için Öneriler

#### Kısa Vadeli
- [ ] Unit test coverage artır (%80+)
- [ ] Integration testleri ekle
- [ ] E2E testleri (Cypress) ekle
- [ ] Logging (ELK Stack) entegrasyonu

#### Orta Vadeli
- [ ] Raporlama modülü (PDF export)
- [ ] Dashboard grafikleri (Chart.js)
- [ ] Email bildirim sistemi
- [ ] Audit log (işlem geçmişi)

#### Uzun Vadeli
- [ ] Multi-tenant support
- [ ] Mobil uygulama (React Native)
- [ ] Barcode/QR kod desteği
- [ ] AI tabanlı stok tahminleme

---

## Ek Kaynaklar

### API Dokümantasyonu
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

### Monitoring
- Health Check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Prometheus: http://localhost:8080/actuator/prometheus

### Kullanışlı Komutlar

```bash
# Backend build
./gradlew clean build

# Backend test
./gradlew test

# Frontend build
cd frontend && npm run build

# Docker build
docker-compose build

# Tüm loglar
docker-compose logs -f

# MySQL'e bağlan
docker exec -it inventory-mysql mysql -uroot -proot

# JWT secret oluştur
openssl rand -base64 64
```

---

**Son Güncelleme:** Aralık 2024
**Versiyon:** 1.0.0
**Durum:** Production Ready
