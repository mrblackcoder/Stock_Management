# 📦 Inventory Management System (IMS)

> Web Tasarım ve Programlama Dersi - Dönem Projesi

## 👨‍🎓 Öğrenci Bilgileri
- **Ad Soyad:** Mehmet Taha Boynikoğlu
- **Öğrenci No:** 212 125 10 34
- **Proje Konusu:** Inventory Management System
- **Geliştirme Dönemi:** 2024-2025 Güz

---

## 📖 Proje Hakkında

**Inventory Management System (IMS)**, modern web teknolojileri kullanılarak geliştirilmiş tam kapsamlı bir envanter yönetim sistemidir. Sistem, işletmelerin ürün, kategori, tedarikçi ve stok işlemlerini gerçek zamanlı olarak takip etmelerini ve yönetmelerini sağlar.

### 🎯 Proje Amaçları
- Güvenli kullanıcı kimlik doğrulama ve yetkilendirme sistemi
- CRUD operasyonları ile tam kapsamlı veri yönetimi
- Dış API entegrasyonu ile gerçek zamanlı veri alışverişi
- Modern, responsive ve kullanıcı dostu arayüz
- RESTful API mimarisi ile ölçeklenebilir backend yapısı


### ✨ Temel Özellikler

#### 🔐 Güvenlik ve Kimlik Doğrulama
- JWT (JSON Web Token) tabanlı kimlik doğrulama
- Rol bazlı erişim kontrolü (ADMIN/USER)
- Şifreli parola saklama (BCrypt)
- Session yönetimi ve token geçerlilik kontrolü

#### 📦 Ürün Yönetimi
- Ürün ekleme, düzenleme, silme ve listeleme (CRUD)
- Ürün arama ve filtreleme
- SKU (Stock Keeping Unit) bazlı takip
- Kategori ve tedarikçi ilişkilendirme
- Stok seviyesi takibi ve düşük stok uyarıları

#### 📂 Kategori Yönetimi
- Kategori oluşturma ve düzenleme
- Kategori bazlı ürün gruplandırma
- Hiyerarşik kategori yapısı desteği

#### 🏢 Tedarikçi Yönetimi
- Tedarikçi bilgilerini kaydetme ve güncelleme
- İletişim bilgileri takibi
- Tedarikçi bazlı ürün listeleme

#### 📊 Stok İşlemleri
- Alış/Satış işlemlerini kaydetme
- Stok giriş/çıkış takibi
- İşlem geçmişi ve raporlama
- Kullanıcı bazlı işlem takibi

#### 🌐 Dış API Entegrasyonu
- Gerçek zamanlı döviz kuru entegrasyonu
- External API ile veri alışverişi
- RESTful servis tüketimi

#### 🎨 Kullanıcı Arayüzü
- Modern ve responsive tasarım
- Single Page Application (SPA) mimarisi
- React Router ile sayfa yönlendirme
- Dinamik veri güncellemeleri
- Kullanıcı dostu form validasyonları


## 🛠️ Teknoloji Yığını

### Backend Teknolojileri
| Teknoloji | Versiyon | Açıklama |
|-----------|----------|----------|
| **Java** | 25 | Ana programlama dili |
| **Spring Boot** | 3.5.7 | Application framework |
| **Spring Security** | 6.2.12 | Güvenlik ve kimlik doğrulama |
| **Spring Data JPA** | 3.5.7 | ORM ve veritabanı işlemleri |
| **Hibernate** | 6.6.33 | JPA implementasyonu |
| **MySQL Connector** | 8.3.0 | MySQL veritabanı bağlantısı |
| **JWT (jjwt)** | 0.12.6 | Token tabanlı kimlik doğrulama |
| **ModelMapper** | 3.2.0 | DTO dönüşümleri |
| **Lombok** | 1.18.36 | Boilerplate kod azaltma |
| **Gradle** | 8.11.1 | Build ve bağımlılık yönetimi |

### Frontend Teknolojileri
| Teknoloji | Versiyon | Açıklama |
|-----------|----------|----------|
| **React** | 18.3.1 | UI framework |
| **React Router DOM** | 7.0.2 | Sayfa yönlendirme |
| **Axios** | 1.7.9 | HTTP istekleri |
| **JavaScript** | ES6+ | Modern JavaScript özellikleri |
| **CSS3** | - | Stil ve düzen |

### Veritabanı
- **MySQL** 8.0.43 - İlişkisel veritabanı yönetim sistemi
- **HikariCP** - Yüksek performanslı connection pooling

### Geliştirme Araçları
- **IntelliJ IDEA** - Java IDE
- **VS Code** - Frontend geliştirme
- **Git** - Versiyon kontrolü
- **Postman** - API test aracı


## 🗄️ Veritabanı Yapısı

Proje **5 ana tablo** ile ilişkisel veritabanı modeli kullanmaktadır:

### 📋 Tablolar ve İlişkiler

#### 1. **users** - Kullanıcılar
```sql
- id (PK, AUTO_INCREMENT)
- username (UNIQUE, NOT NULL)
- email (UNIQUE, NOT NULL)
- password (NOT NULL, BCrypt Encrypted)
- role (ADMIN/USER)
- created_at (TIMESTAMP)
```

#### 2. **categories** - Kategoriler
```sql
- id (PK, AUTO_INCREMENT)
- name (UNIQUE, NOT NULL)
- description (TEXT)
```
**İlişki:** One-to-Many → Products

#### 3. **suppliers** - Tedarikçiler
```sql
- id (PK, AUTO_INCREMENT)
- name (NOT NULL)
- contact_person (VARCHAR)
- phone (VARCHAR)
- email (VARCHAR)
- address (TEXT)
```
**İlişki:** One-to-Many → Products

#### 4. **products** - Ürünler
```sql
- id (PK, AUTO_INCREMENT)
- name (NOT NULL)
- description (TEXT)
- sku (UNIQUE, NOT NULL)
- price (DECIMAL)
- quantity (INTEGER, NOT NULL)
- category_id (FK → categories)
- supplier_id (FK → suppliers)
- created_at (TIMESTAMP)
```
**İlişkiler:** 
- Many-to-One → Categories
- Many-to-One → Suppliers
- One-to-Many → Stock Transactions

#### 5. **stock_transactions** - Stok İşlemleri
```sql
- id (PK, AUTO_INCREMENT)
- product_id (FK → products)
- user_id (FK → users)
- transaction_type (PURCHASE/SALE/ADJUSTMENT)
- quantity (INTEGER, NOT NULL)
- notes (TEXT)
- transaction_date (TIMESTAMP)
```
**İlişkiler:**
- Many-to-One → Products
- Many-to-One → Users

### 🔗 Entity İlişki Diyagramı (ERD)
```
users (1) ----< (N) stock_transactions (N) >---- (1) products
                                                        |
                                                        |
categories (1) ----< (N) products (N) >---- (1) suppliers
```


## 🚀 Kurulum ve Çalıştırma

### 📋 Ön Gereksinimler
Aşağıdaki yazılımların sisteminizde yüklü olması gerekmektedir:

| Yazılım | Minimum Versiyon | Önerilen Versiyon |
|---------|------------------|-------------------|
| Java JDK | 17+ | 21+ |
| Node.js | 16+ | 18+ |
| MySQL | 8.0+ | 8.0.43 |
| Git | 2.0+ | Son versiyon |

---

## 🎯 HIZLI BAŞLANGIÇ - SİSTEM ÇALIŞTIRMA

> **ÖNEMLİ:** Uygulamayı çalıştırmadan önce aşağıdaki adımları sırayla takip edin!

### ⚠️ Çalıştırma Sırası (ÇOK ÖNEMLİ!)

Sistem **mutlaka** aşağıdaki sırada başlatılmalıdır:

1. **MySQL Veritabanı** (İlk önce)
2. **Backend API** (İkinci)
3. **Frontend React App** (En son)

---

### 🗄️ ADIM 1: MySQL Veritabanı Kurulumu

#### MySQL Servisini Başlatma
```bash
# Linux/Mac
sudo service mysql start
# veya
sudo systemctl start mysql

# Windows
net start mysql
```

#### Veritabanını Oluşturma
```bash
# MySQL'e root olarak giriş yapın
mysql -u root -p
```

```sql
-- Veritabanını oluştur
CREATE DATABASE IF NOT EXISTS inventory_management_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Çıkış
EXIT;
```

#### Veritabanı Ayarlarını Kontrol Etme
`src/main/resources/application.properties` dosyasını açın ve kontrol edin:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
```
> **Not:** MySQL şifreniz farklıysa `password` değerini güncelleyin!

---

### 🔧 ADIM 2: Backend (Spring Boot) Başlatma

#### İlk Kurulum (Sadece bir kez)
```bash
# Proje dizinine gidin
cd /home/taha/IdeaProjects/StockManagement

# Gradle wrapper'a yürütme izni verin (Linux/Mac)
chmod +x gradlew

# Build işlemini gerçekleştirin
./gradlew clean build -x test
```

#### Backend'i Çalıştırma (ÖNEMLİ!)

**Terminal 1'de aşağıdaki komutu çalıştırın:**
```bash
cd /home/taha/IdeaProjects/StockManagement
./gradlew bootRun
```

**✅ Başarı Mesajları:**
```
Started StockManagementApplication in X.XXX seconds
Tomcat started on port 8080
```

**🌐 Backend Erişim:**
- API Base URL: http://localhost:8080
- API Status: http://localhost:8080/api
- Swagger (eğer aktifse): http://localhost:8080/swagger-ui.html

> **⚠️ DİKKAT:** Backend tamamen başlayana kadar (yaklaşık 15-20 saniye) frontend'i başlatmayın!

---

### 🎨 ADIM 3: Frontend (React) Başlatma

#### İlk Kurulum (Sadece bir kez)
```bash
# Frontend dizinine gidin
cd /home/taha/IdeaProjects/StockManagement/frontend

# Node.js bağımlılıklarını yükleyin
npm install
```

#### Frontend'i Çalıştırma (ÖNEMLİ!)

**Terminal 2'de aşağıdaki komutu çalıştırın:**
```bash
cd /home/taha/IdeaProjects/StockManagement/frontend
npm start
```

**✅ Başarı Mesajları:**
```
Compiled successfully!
You can now view frontend in the browser.
Local: http://localhost:3000
```

**🌐 Frontend Erişim:**
- Ana Sayfa: http://localhost:3000
- Login Sayfası: http://localhost:3000/login
- Dashboard: http://localhost:3000/dashboard

> **⚠️ NOT:** Tarayıcı otomatik açılmazsa manuel olarak http://localhost:3000 adresine gidin.

---

### 🔄 KAPSAMLI ÇALIŞTIRMA KOMUTLARI

#### Tüm Sistemi Sıfırdan Başlatma (TAM KOMUT)

```bash
# 1. MySQL'i başlat
sudo service mysql start

# 2. Eski process'leri temizle
cd /home/taha/IdeaProjects/StockManagement
lsof -ti:8080 | xargs -r kill -9
lsof -ti:3000 | xargs -r kill -9

# 3. Backend'i başlat (Terminal 1)
./gradlew bootRun &

# 4. 15 saniye bekle (Backend'in başlaması için)
sleep 15

# 5. Frontend'i başlat (Terminal 2)
cd frontend
npm start
```

#### Hızlı Yeniden Başlatma
```bash
# Backend'i durdur ve yeniden başlat
pkill -9 -f "gradle"
./gradlew bootRun &

# Frontend'i durdur ve yeniden başlat
pkill -9 -f "react-scripts"
cd frontend && npm start
```

---

### ✅ Kurulum Doğrulama

#### 1. Backend Kontrolü
```bash
# API status kontrolü
curl http://localhost:8080/api

# Beklenen çıktı:
# {"status":"running","message":"Inventory Management System API is running","version":"1.0.0","endpoints":{...}}
```

#### 2. Frontend Kontrolü
```bash
# Frontend erişim kontrolü
curl -I http://localhost:3000

# Beklenen durum kodu: 200 OK
```

#### 3. Veritabanı Kontrolü
```sql
mysql -u root -p
USE inventory_management_db;
SHOW TABLES;

-- Beklenen tablolar:
-- users, products, categories, suppliers, stock_transactions
```

#### 4. Manuel Tarayıcı Testi
1. http://localhost:3000 adresine gidin
2. Login sayfası görünmeli
3. Test kullanıcısı ile giriş yapın:
   - **Username:** `admin`
   - **Password:** `admin123`
4. Dashboard açılmalı

---

### ❌ Sık Karşılaşılan Sorunlar ve Çözümleri

#### Port Zaten Kullanımda Hatası
```bash
# Hata: "Address already in use: bind"
# Çözüm: Port'u kullanan process'i öldürün

# 8080 portunu temizle (Backend)
lsof -ti:8080 | xargs -r kill -9

# 3000 portunu temizle (Frontend)
lsof -ti:3000 | xargs -r kill -9
```

#### MySQL Bağlantı Hatası
```bash
# Hata: "Access denied for user 'root'@'localhost'"
# Çözüm: application.properties'deki şifreyi kontrol edin

# MySQL şifrenizi öğrenin/değiştirin
sudo mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED BY 'yeni_sifre';
FLUSH PRIVILEGES;
EXIT;
```

#### Frontend Backend'e Bağlanamıyor
```bash
# Hata: "Network Error" veya "CORS Error"
# Çözüm: Backend'in çalıştığından emin olun

# Backend durumunu kontrol et
curl http://localhost:8080/api

# Backend yoksa başlat
cd /home/taha/IdeaProjects/StockManagement
./gradlew bootRun
```

---

### 🛑 Sistemi Durdurma

#### Güvenli Durdurma
```bash
# Backend'i durdur (Terminal 1'de Ctrl+C)
# veya
pkill -9 -f "gradle"

# Frontend'i durdur (Terminal 2'de Ctrl+C)
# veya
pkill -9 -f "react-scripts"

# MySQL'i durdur (opsiyonel)
sudo service mysql stop
```

---

## 📦 Production Build (Canlı Ortam için)

### Backend JAR Dosyası Oluşturma
```bash
cd /home/taha/IdeaProjects/StockManagement
./gradlew clean bootJar

# JAR dosyası şurada oluşur:
# build/libs/StockManagement-0.0.1-SNAPSHOT.jar

# JAR'ı çalıştırma
java -jar build/libs/StockManagement-0.0.1-SNAPSHOT.jar
```

### Frontend Production Build
```bash
cd /home/taha/IdeaProjects/StockManagement/frontend
npm run build

# Build dosyaları şurada oluşur:
# frontend/build/

# Static server ile çalıştırma
npx serve -s build -l 3000
```


## 📱 Kullanım Kılavuzu

### 🔑 Test Kullanıcıları
Sistem ilk çalıştırmada aşağıdaki test kullanıcılarını otomatik oluşturur:

| Kullanıcı Adı | Şifre | Rol | Açıklama |
|---------------|-------|-----|----------|
| `admin` | `admin123` | ADMIN | Tüm yetkilere sahip |
| `user` | `user123` | USER | Sınırlı yetkiler |

### 🌐 API Endpoints

#### 🔐 Kimlik Doğrulama (`/api/auth`)
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123",
  "role": "USER"
}

Response: 201 Created
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "newuser",
  "role": "USER"
}
```

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "admin",
  "role": "ADMIN"
}
```

#### 📦 Ürün İşlemleri (`/api/products`)
```http
# Tüm ürünleri listele
GET /api/products
Authorization: Bearer {token}

# ID ile ürün getir
GET /api/products/{id}
Authorization: Bearer {token}

# Yeni ürün ekle (ADMIN)
POST /api/products
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Laptop",
  "description": "High performance laptop",
  "sku": "LAP-001",
  "price": 15000.00,
  "quantity": 10,
  "categoryId": 1,
  "supplierId": 1
}

# Ürün güncelle (ADMIN)
PUT /api/products/{id}
Authorization: Bearer {token}
Content-Type: application/json

# Ürün sil (ADMIN)
DELETE /api/products/{id}
Authorization: Bearer {token}

# Ürün ara
GET /api/products/search?keyword=laptop
Authorization: Bearer {token}

# Düşük stoklu ürünler
GET /api/products/low-stock
Authorization: Bearer {token}
```

#### 📂 Kategori İşlemleri (`/api/categories`)
```http
# Tüm kategorileri listele
GET /api/categories
Authorization: Bearer {token}

# Yeni kategori ekle (ADMIN)
POST /api/categories
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}

# Kategori güncelle (ADMIN)
PUT /api/categories/{id}
Authorization: Bearer {token}

# Kategori sil (ADMIN)
DELETE /api/categories/{id}
Authorization: Bearer {token}
```

#### 🏢 Tedarikçi İşlemleri (`/api/suppliers`)
```http
# Tüm tedarikçileri listele
GET /api/suppliers
Authorization: Bearer {token}

# Yeni tedarikçi ekle (ADMIN)
POST /api/suppliers
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Tech Supplier Inc.",
  "contactPerson": "John Doe",
  "phone": "+90 555 123 4567",
  "email": "contact@techsupplier.com",
  "address": "Istanbul, Turkey"
}
```

#### 📊 Stok İşlemleri (`/api/transactions`)
```http
# Tüm işlemleri listele
GET /api/transactions
Authorization: Bearer {token}

# Yeni işlem ekle
POST /api/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 1,
  "transactionType": "PURCHASE",
  "quantity": 50,
  "notes": "Yeni stok girişi"
}

# Ürüne göre işlem geçmişi
GET /api/transactions/product/{productId}
Authorization: Bearer {token}
```

#### 👤 Kullanıcı İşlemleri (`/api/users`)
```http
# Profil bilgisi
GET /api/users/profile
Authorization: Bearer {token}

# Kullanıcı güncelle
PUT /api/users/profile
Authorization: Bearer {token}
```

### 🖥️ Frontend Kullanımı

1. **Giriş Yapma**
   - http://localhost:3000 adresine gidin
   - Kullanıcı adı ve şifre ile giriş yapın

2. **Dashboard**
   - Genel istatistikler
   - Son işlemler
   - Düşük stok uyarıları

3. **Ürün Yönetimi**
   - Ürün listesini görüntüleme
   - Yeni ürün ekleme (Admin)
   - Ürün düzenleme ve silme (Admin)

4. **Kategori Yönetimi**
   - Kategori ekleme/düzenleme/silme (Admin)

5. **Tedarikçi Yönetimi**
   - Tedarikçi bilgilerini yönetme (Admin)

6. **Stok İşlemleri**
   - Alış/Satış işlemi kaydetme
   - İşlem geçmişini görüntüleme


## 📁 Proje Yapısı

```
Stock_Management/
│
├── 📂 src/main/
│   ├── 📂 java/com/ims/stockmanagement/
│   │   ├── 📂 config/              # Yapılandırma sınıfları
│   │   │   ├── CorsConfig.java
│   │   │   ├── ModelMapperConfig.java
│   │   │   └── SwaggerConfig.java
│   │   │
│   │   ├── 📂 controllers/         # REST API Controllers
│   │   │   ├── AuthController.java
│   │   │   ├── CategoryController.java
│   │   │   ├── ProductController.java
│   │   │   ├── SupplierController.java
│   │   │   ├── TransactionController.java
│   │   │   └── UserController.java
│   │   │
│   │   ├── 📂 dtos/                # Data Transfer Objects
│   │   │   ├── CategoryDTO.java
│   │   │   ├── ProductDTO.java
│   │   │   ├── SupplierDTO.java
│   │   │   ├── TransactionDTO.java
│   │   │   ├── UserDTO.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   └── ResponseDTO.java
│   │   │
│   │   ├── 📂 entities/            # JPA Entity Models
│   │   │   ├── User.java
│   │   │   ├── Product.java
│   │   │   ├── Category.java
│   │   │   ├── Supplier.java
│   │   │   └── StockTransaction.java
│   │   │
│   │   ├── 📂 enums/               # Enum Sınıfları
│   │   │   ├── UserRole.java
│   │   │   ├── TransactionType.java
│   │   │   └── TransactionStatus.java
│   │   │
│   │   ├── 📂 exceptions/          # Custom Exception Classes
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── NotFoundException.java
│   │   │   ├── AlreadyExistsException.java
│   │   │   ├── InvalidCredentialsException.java
│   │   │   └── InsufficientStockException.java
│   │   │
│   │   ├── 📂 repositories/        # JPA Repositories
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   ├── CategoryRepository.java
│   │   │   ├── SupplierRepository.java
│   │   │   └── StockTransactionRepository.java
│   │   │
│   │   ├── 📂 security/            # Security & JWT
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtService.java
│   │   │   └── SecurityConfig.java
│   │   │
│   │   ├── 📂 services/            # Business Logic Layer
│   │   │   ├── AuthService.java
│   │   │   ├── CategoryService.java
│   │   │   ├── ProductService.java
│   │   │   ├── SupplierService.java
│   │   │   ├── StockTransactionService.java
│   │   │   ├── UserService.java
│   │   │   └── ExternalApiService.java
│   │   │
│   │   └── StockManagementApplication.java
│   │
│   └── 📂 resources/
│       ├── application.properties   # Ana yapılandırma
│       ├── application-dev.properties
│       ├── application-prod.properties
│       └── 📂 templates/           # Thymeleaf Templates
│           ├── login.html
│           ├── register.html
│           └── dashboard.html
│
├── 📂 frontend/                    # React Frontend
│   ├── 📂 public/
│   │   ├── index.html
│   │   ├── favicon.ico
│   │   └── manifest.json
│   │
│   ├── 📂 src/
│   │   ├── 📂 components/         # React Components
│   │   │   └── Layout.js
│   │   │
│   │   ├── 📂 pages/              # React Pages
│   │   │   ├── LoginPage.js
│   │   │   ├── RegisterPage.js
│   │   │   ├── DashboardPage.js
│   │   │   ├── ProductPage.js
│   │   │   ├── CategoryPage.js
│   │   │   ├── SupplierPage.js
│   │   │   ├── TransactionPage.js
│   │   │   └── ProfilePage.js
│   │   │
│   │   ├── 📂 service/            # API Services
│   │   │   ├── ApiService.js
│   │   │   └── Guard.js
│   │   │
│   │   ├── App.js                 # Main App Component
│   │   ├── App.css
│   │   ├── index.js
│   │   └── index.css
│   │
│   ├── package.json
│   └── package-lock.json
│
├── 📂 gradle/                      # Gradle Wrapper
├── build.gradle                    # Gradle Build Script
├── settings.gradle
├── gradlew                         # Gradle Wrapper Script (Unix)
├── gradlew.bat                     # Gradle Wrapper Script (Windows)
│
├── 📄 README.md                    # Proje Dokümantasyonu
├── 📄 PROJE_DURUMU.md             # Proje Durum Raporu
├── 📄 HIZLI_BAŞLANGIÇ.md          # Hızlı Başlangıç Kılavuzu
├── 📄 SONRAKI_ADIMLAR.md          # Gelecek Planlama
├── 📄 .gitignore                  # Git Ignore
└── 📄 GIT_GUIDE.md                # Git Kullanım Kılavuzu
```

### 🏗️ Mimari Katmanlar

#### Backend Mimarisi (Layered Architecture)
```
┌─────────────────────────────────────┐
│   Controllers (REST Endpoints)      │  ← HTTP Requests
├─────────────────────────────────────┤
│   DTOs (Data Transfer Objects)      │  ← Data Transformation
├─────────────────────────────────────┤
│   Services (Business Logic)         │  ← Core Logic
├─────────────────────────────────────┤
│   Repositories (Data Access)        │  ← Database Operations
├─────────────────────────────────────┤
│   Entities (Database Models)        │  ← JPA Entities
├─────────────────────────────────────┤
│   MySQL Database                    │  ← Persistent Storage
└─────────────────────────────────────┘
```

#### Frontend Mimarisi (Component-Based)
```
┌─────────────────────────────────────┐
│   Pages (Route Components)          │  ← User Interface
├─────────────────────────────────────┤
│   Components (Reusable UI)          │  ← UI Building Blocks
├─────────────────────────────────────┤
│   Services (API Communication)      │  ← Backend Integration
├─────────────────────────────────────┤
│   Guards (Auth Protection)          │  ← Route Protection
└─────────────────────────────────────┘
```


## ✅ Proje Gereksinimleri Karşılama Durumu

### Temel Fonksiyonel Gereksinimler
- ✅ **5+ Veritabanı Tablosu** - 5 tablo (users, products, categories, suppliers, stock_transactions)
- ✅ **İlişkisel Tablo Yapısı** - One-to-Many, Many-to-One ilişkiler
- ✅ **Login & Register** - JWT bazlı kimlik doğrulama
- ✅ **CRUD Operasyonları** - Tüm tablolar için Create, Read, Update, Delete
- ✅ **External API Entegrasyonu** - Döviz kuru API'si entegrasyonu
- ✅ **Uzaktan Erişim** - REST API endpoints
- ✅ **Embedded Interface** - Thymeleaf template engine

### Teknik Mimari ve Teknoloji Yığını
- ✅ **Java 19+** - Java 25 kullanılmıştır
- ✅ **Spring Boot 3.x** - Spring Boot 3.5.7
- ✅ **JavaScript ES6+** - Modern JavaScript özellikleri
- ✅ **Thymeleaf** - Server-side rendering için
- ✅ **React.js** - SPA frontend için
- ✅ **MySQL** - İlişkisel veritabanı

### Güvenlik ve Deployment
- ✅ **Güvenlik** - Spring Security + JWT
- ✅ **Rol Bazlı Erişim** - ADMIN/USER rolleri
- ⏳ **AWS Deployment** - Deployment hazırlıkları tamamlandı

### GitHub Kullanımı
- ✅ **Private Repository** - GitHub'da private repo
- ✅ **Düzenli Commitler** - 20+ anlamlı commit
- ✅ **Commit Mesajları** - Standart commit message convention
- ✅ **Geliştirme Süreci** - Adım adım development

## 🔒 Güvenlik Özellikleri

- 🔐 **JWT Token Authentication** - Stateless authentication
- 🛡️ **BCrypt Password Encryption** - Güvenli şifre saklama
- 👥 **Role-Based Access Control (RBAC)** - ADMIN/USER rolleri
- 🚫 **CORS Configuration** - Cross-origin güvenliği
- 🔑 **Session Management** - Token tabanlı session yönetimi
- 📝 **SQL Injection Protection** - JPA/Hibernate ile güvenli sorgular
- 🛑 **XSS Protection** - Input validation ve sanitization

## 🚀 Gelecek Geliştirmeler

- [ ] **Docker Containerization** - Docker ve Docker Compose desteği
- [ ] **Unit & Integration Tests** - Kapsamlı test coverage
- [ ] **API Documentation** - Swagger/OpenAPI entegrasyonu
- [ ] **Logging System** - SLF4J ve Logback yapılandırması
- [ ] **Email Notifications** - Düşük stok uyarıları için
- [ ] **Export Features** - Excel/PDF rapor çıktıları
- [ ] **Advanced Reporting** - Grafik ve istatistikler
- [ ] **Multi-language Support** - i18n desteği
- [ ] **PWA Support** - Progressive Web App özellikleri
- [ ] **Real-time Updates** - WebSocket entegrasyonu

## 🐛 Bilinen Sorunlar ve Çözümler

### Problem: Port Already in Use
```bash
# Çözüm: 8080 portunu kullanan processi sonlandır
lsof -ti:8080 | xargs kill -9
```

### Problem: MySQL Connection Refused
```bash
# Çözüm: MySQL servisini başlat
sudo systemctl start mysql
# veya
sudo service mysql start
```

### Problem: JWT Token Expired
```
Çözüm: Yeni login yaparak token yenileyin
```

## 📚 Kaynaklar ve Referanslar

### Resmi Dokümantasyonlar
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React Documentation](https://react.dev/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [JWT.io](https://jwt.io/)

### Faydalı Araçlar
- [Postman](https://www.postman.com/) - API testing
- [MySQL Workbench](https://www.mysql.com/products/workbench/) - Database management
- [Git](https://git-scm.com/) - Version control

## 👨‍💻 Geliştirici Notları

### Commit Convention
```
feat: Yeni özellik ekleme
fix: Bug düzeltme
docs: Dokümantasyon değişikliği
style: Kod formatı değişikliği
refactor: Kod refactoring
test: Test ekleme/düzeltme
chore: Bakım işleri
```

### Branch Strategy
```
main: Production-ready kod
develop: Development branch
feature/*: Yeni özellikler
bugfix/*: Bug düzeltmeleri
```

## 📄 Lisans

Bu proje eğitim amaçlı geliştirilmiştir.  
**Web Tasarım ve Programlama Dersi - Dönem Projesi**

---

## 📞 İletişim

**Mehmet Taha Boynikoğlu**  
Öğrenci No: 212 125 10 34  
GitHub: [@mrblackcoder](https://github.com/mrblackcoder)

---

<div align="center">
  
**⭐ Bu projeyi faydalı bulduysanız yıldız vermeyi unutmayın!**

Made with ❤️ for Web Design and Programming Course

</div>
