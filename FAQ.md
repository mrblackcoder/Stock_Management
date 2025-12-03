# Frequently Asked Questions (FAQ)
## Stock Management System - Sık Sorulan Sorular

**Proje:** Inventory Management System  
**Öğrenci:** Mehmet Taha Boynikoğlu (212 125 10 34)  
**Tarih:** Aralık 2024

---

## İçindekiler

1. [Genel Sorular](#genel-sorular)
2. [Kurulum ve Yapılandırma](#kurulum-ve-yapılandırma)
3. [Authentication ve Security](#authentication-ve-security)
4. [Kullanıcı İşlemleri](#kullanıcı-işlemleri)
5. [Ürün Yönetimi](#ürün-yönetimi)
6. [Stok İşlemleri](#stok-işlemleri)
7. [Hata Çözümleri](#hata-çözümleri)
8. [Performans ve Optimizasyon](#performans-ve-optimizasyon)
9. [Deployment ve Production](#deployment-ve-production)
10. [AWS Cloud](#aws-cloud)

---

## Genel Sorular

### S1: Bu sistem ne işe yarar?

**Cevap:** Stock Management System (Envanter Yönetim Sistemi), işletmelerin ürün stoklarını dijital ortamda yönetmelerine olanak sağlayan kapsamlı bir web uygulamasıdır. Sistem ile:
- Ürün envanter takibi
- Kategori ve tedarikçi yönetimi
- Stok alış/satış/düzeltme işlemleri
- Düşük stok uyarıları
- Gerçek zamanlı dashboard istatistikleri
- Rol tabanlı kullanıcı erişimi

gibi işlevleri gerçekleştirebilirsiniz.

---

### S2: Sistemin teknik özellikleri nelerdir?

**Cevap:**

**Backend:**
- Spring Boot 3.5.7 (Java 21)
- Spring Security 6 (JWT Authentication)
- Spring Data JPA (Hibernate)
- MySQL 8.0 Database
- RESTful API Architecture

**Frontend:**
- React 19.2.0
- React Router 7.1.1
- Axios (HTTP Client)
- Bootstrap 5.3.3
- CryptoJS (Client-side encryption)

**Security:**
- JWT Access Token (15 dakika)
- JWT Refresh Token (7 gün)
- BCrypt Password Hashing
- Role-Based Access Control (ADMIN, USER)

---

### S3: Sistemde hangi roller var ve farklılıkları nedir?

**Cevap:**

**ADMIN (Yönetici):**
- Tüm ürünleri görüntüleme, ekleme, düzenleme, silme
- Kategori ve tedarikçi yönetimi (CRUD)
- Stok işlemleri yapma (alış, satış, düzeltme)
- Tüm raporları görüntüleme
- Dashboard istatistiklerini görme
- Düşük stok uyarılarına müdahale

**USER (Kullanıcı):**
- Ürünleri sadece görüntüleme (read-only)
- Dashboard istatistiklerini görme
- Düşük stok uyarılarını görme
- Kendi profil bilgilerini güncelleme
- Ürün arama ve filtreleme
- CRUD işlemleri yapamaz

---

### S4: Sistemin lisansı nedir?

**Cevap:** Bu proje, Fatih Sultan Mehmet Vakıf Üniversitesi Web Tasarımı ve Programlama dersi için geliştirilmiş bir akademik projedir. Ticari kullanım için yazar ile iletişime geçilmesi önerilir.

---

## Kurulum ve Yapılandırma

### S5: Sistem nasıl kurulur?

**Cevap:**

**Adım 1: Gereksinimleri Yükleyin**
```bash
# Java 21 (Amazon Corretto önerilir)
# MySQL 8.0
# Node.js 18+
# Git
```

**Adım 2: Projeyi İndirin**
```bash
git clone https://github.com/mrblackcoder/Stock_Management.git
cd Stock_Management
```

**Adım 3: Veritabanını Oluşturun**
```bash
mysql -u root -p
CREATE DATABASE inventory_management_db;
exit;
```

**Adım 4: Backend Yapılandırması**
```bash
# src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

**Adım 5: Backend'i Başlatın**
```bash
./gradlew bootRun
# veya custom DB password ile:
DB_PASSWORD='YourPassword' ./gradlew bootRun
```

**Adım 6: Frontend'i Başlatın**
```bash
cd frontend
npm install
npm start
```

**Adım 7: Uygulamaya Erişin**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

---

### S6: application.properties dosyasını nasıl yapılandırmalıyım?

**Cevap:**

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management_db
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Server Configuration
server.port=8080
server.error.include-message=always
server.error.include-stacktrace=never

# JWT Configuration
jwt.secret=your-256-bit-secret-key-change-this-in-production
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000

# Logging
logging.level.com.ims.stockmanagement=INFO
logging.level.org.springframework.security=DEBUG
```

**Önemli Notlar:**
- `jwt.secret` mutlaka güçlü bir değerle değiştirilmeli (production)
- `spring.jpa.show-sql=true` sadece development'ta kullanın
- `DB_PASSWORD` environment variable ile set edilebilir

---

### S7: İlk admin kullanıcısı nasıl oluşturulur?

**Cevap:** Sistem ilk çalıştırıldığında `DataInitializer.java` komponenti otomatik olarak admin kullanıcısını oluşturur:

**Varsayılan Admin Hesabı:**
- Kullanıcı Adı: `admin`
- Şifre: `Admin@123!Secure`
- Rol: ADMIN

**Önemli:** İlk girişten sonra güvenlik için şifrenizi mutlaka değiştirin!

**Manuel Admin Oluşturma (SQL):**
```sql
-- Şifre: Admin@123!Secure (BCrypt hash)
INSERT INTO users (username, password, full_name, role) 
VALUES (
    'admin', 
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyFUHc1TjyeG',
    'Admin User',
    'ADMIN'
);
```

---

### S8: Frontend API endpoint'lerini nasıl yapılandırırım?

**Cevap:** `frontend/src/service/ApiService.js` dosyasında:

```javascript
const BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
```

**Development:**
```bash
# .env.development
REACT_APP_API_URL=http://localhost:8080/api
```

**Production:**
```bash
# .env.production
REACT_APP_API_URL=https://api.yourdomain.com/api
```

---

## Authentication ve Security

### S9: JWT token'ları nasıl çalışır?

**Cevap:**

**Access Token (15 dakika):**
- Her API isteğinde `Authorization: Bearer <token>` header'ında gönderilir
- Kullanıcı kimliği ve rolü içerir
- 15 dakika sonra expire olur
- Kısa ömürlü, güvenlik için

**Refresh Token (7 gün):**
- Veritabanında saklanır
- Access token yenilemek için kullanılır
- 7 gün geçerli
- Logout yapıldığında revoke edilir

**Token Flow:**
```
1. Login → Access Token + Refresh Token
2. API İsteği → Access Token kullan
3. Access Token Expire → Refresh Token ile yenile
4. Yeni Access Token Al → API İsteğine devam
5. Logout → Her iki token'ı iptal et
```

---

### S10: Token'lar nasıl saklanır?

**Cevap:** Frontend'de token'lar şifreli olarak localStorage'da saklanır:

```javascript
import CryptoJS from 'crypto-js';

const SECRET_KEY = 'your-secret-key';

// Token'ı şifrele ve sakla
const encryptedToken = CryptoJS.AES.encrypt(
    accessToken,
    SECRET_KEY
).toString();
localStorage.setItem('accessToken', encryptedToken);

// Token'ı al ve decrypt et
const encryptedToken = localStorage.getItem('accessToken');
const token = CryptoJS.AES.decrypt(
    encryptedToken,
    SECRET_KEY
).toString(CryptoJS.enc.Utf8);
```

**Güvenlik İpuçları:**
- Production'da SECRET_KEY'i environment variable'dan alın
- HTTPS kullanın
- HttpOnly cookie kullanımı daha güvenlidir (gelecek versiyonlarda)

---

### S11: Şifre politikası nasıldır?

**Cevap:**

**Gereksinimler:**
- Minimum 8 karakter
- En az 1 büyük harf (A-Z)
- En az 1 küçük harf (a-z)
- En az 1 rakam (0-9)
- En az 1 özel karakter (!@#$%^&*)

**Örnekler:**
- ✅ `Admin@123!Secure`
- ✅ `MyPass123!`
- ✅ `Secure$2024`
- ❌ `admin123` (büyük harf ve özel karakter yok)
- ❌ `Admin123` (özel karakter yok)
- ❌ `Admin@` (rakam yok, kısa)

**Backend Validation:**
```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password requirements not met"
)
```

---

### S12: Token refresh nasıl çalışır?

**Cevap:**

**Otomatik Refresh (Frontend):**
```javascript
// Axios Response Interceptor
axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            
            try {
                // Refresh token ile yeni access token al
                const newAccessToken = await refreshToken();
                
                // Başarısız isteği yeni token ile tekrarla
                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                return axiosInstance(originalRequest);
            } catch (refreshError) {
                // Refresh başarısız, logout yap
                logout();
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);
```

**Manuel Refresh:**
```bash
POST /api/auth/refresh
Content-Type: application/json

{
    "refreshToken": "your-refresh-token-here"
}

Response:
{
    "accessToken": "new-access-token",
    "refreshToken": "same-refresh-token",
    "tokenType": "Bearer"
}
```

---

### S13: CORS hatası nasıl çözülür?

**Cevap:**

**Backend (SecurityConfig.java):**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "https://yourdomain.com"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
}
```

**Production için:**
- Frontend domain'ini `allowedOrigins`'e ekleyin
- Wildcard (*) kullanmaktan kaçının
- `allowCredentials(true)` ile cookie/header gönderimi yapılabilir

---

## Kullanıcı İşlemleri

### S14: Yeni kullanıcı nasıl kaydedilir?

**Cevap:**

**Frontend (Register Page):**
1. http://localhost:3000/register adresine gidin
2. Formu doldurun:
   - Kullanıcı Adı (3-20 karakter, benzersiz)
   - Şifre (güçlü şifre gereksinimleri)
   - Tam Ad
3. "Register" butonuna tıklayın
4. Otomatik olarak USER rolü ile kayıt olursunuz

**API:**
```bash
POST /api/auth/register
Content-Type: application/json

{
    "username": "newuser",
    "password": "SecurePass@123",
    "fullName": "John Doe"
}

Response (201 CREATED):
{
    "id": 2,
    "username": "newuser",
    "fullName": "John Doe",
    "role": "USER",
    "createdAt": "2024-12-04T10:30:00"
}
```

**Not:** Güvenlik nedeniyle kullanıcılar kayıt sırasında rol seçemez. Varsayılan olarak USER rolü atanır. Admin rolü sadece mevcut admin tarafından veya veritabanından manuel olarak atanabilir.

---

### S15: Kullanıcı rolü nasıl değiştirilir?

**Cevap:**

**Yöntem 1: SQL ile Manuel**
```sql
UPDATE users 
SET role = 'ADMIN' 
WHERE username = 'newuser';
```

**Yöntem 2: Future Feature**
Gelecek versiyonlarda admin kullanıcıları için kullanıcı yönetim paneli eklenecektir:
- Admin panelinden kullanıcı listesi
- Rol değiştirme butonu
- Kullanıcı aktif/pasif etme

**Mevcut Durum:**
Rol değişikliği için:
1. MySQL Workbench/DBeaver gibi araç kullanın
2. `users` tablosunda ilgili kullanıcının `role` alanını güncelleyin
3. Kullanıcı logout/login yaparak yeni rolü kullanabilir

---

### S16: Şifre nasıl değiştirilir?

**Cevap:**

**Frontend (Profile Page):**
1. Sol menüden "Profile" sayfasına gidin
2. "Edit Profile" butonuna tıklayın
3. "New Password" alanına yeni şifrenizi girin
4. Şifre gereksinimlerini karşıladığından emin olun
5. "Save Changes" butonuna tıklayın
6. Başarılı mesajı görüntülenecektir

**API:**
```bash
PUT /api/users/me
Authorization: Bearer <access_token>
Content-Type: application/json

{
    "password": "NewSecurePass@123"
}
```

**Şifre Sıfırlama (Unutma Durumu):**
Mevcut versiyonda "Şifremi Unuttum" özelliği yoktur. Şifre sıfırlama için:
1. Admin ile iletişime geçin
2. Admin veritabanından şifrenizi sıfırlayabilir

---

## Ürün Yönetimi

### S17: Ürün nasıl eklenir?

**Cevap:** (Sadece Admin)

**Frontend:**
1. "Products" sayfasına gidin
2. "Add New Product" butonuna tıklayın
3. Formu doldurun:
   - Ürün Adı (zorunlu, benzersiz)
   - SKU (zorunlu, benzersiz)
   - Açıklama (isteğe bağlı)
   - Kategori (dropdown'dan seçin)
   - Tedarikçi (dropdown'dan seçin)
   - Fiyat (pozitif sayı)
   - Stok Miktarı (0 veya pozitif)
   - Düşük Stok Eşiği (pozitif sayı)
4. "Create Product" butonuna tıklayın

**API:**
```bash
POST /api/products
Authorization: Bearer <admin_token>
Content-Type: application/json

{
    "name": "Wireless Mouse",
    "sku": "MOU-001",
    "description": "Ergonomic wireless mouse",
    "price": 29.99,
    "quantityInStock": 100,
    "lowStockThreshold": 20,
    "categoryId": 1,
    "supplierId": 2
}
```

---

### S18: SKU nedir ve nasıl oluşturulur?

**Cevap:**

**SKU (Stock Keeping Unit):** Ürünleri benzersiz şekilde tanımlayan stok kodu.

**Best Practices:**
```
Format: [CATEGORY]-[TYPE]-[NUMBER]

Örnekler:
- ELEC-LAP-001 (Electronics - Laptop - 001)
- FURN-DESK-042 (Furniture - Desk - 042)
- STAT-PEN-123 (Stationery - Pen - 123)
```

**Özellikler:**
- Benzersiz olmalı (unique constraint)
- Maksimum 50 karakter
- Büyük/küçük harf duyarlı
- Boşluk içermemeli (tire veya alt çizgi kullanın)

**Sistem Tarafından Kontrol:**
- SKU mevcut mu? → Hata döner
- Boş mu? → Validation hatası

---

### S19: Ürün kategorisi veya tedarikçisi nasıl değiştirilir?

**Cevap:**

**Frontend:**
1. "Products" sayfasında ilgili ürünün "Edit" butonuna tıklayın
2. "Category" dropdown'ından yeni kategori seçin
3. "Supplier" dropdown'ından yeni tedarikçi seçin
4. "Update Product" butonuna tıklayın

**API:**
```bash
PUT /api/products/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
    "categoryId": 2,
    "supplierId": 3
}
```

**Not:** Kategori veya tedarikçi değişikliği:
- Ürünün stok miktarını etkilemez
- İşlem geçmişi korunur
- Diğer alanlar değişmez (sadece gönderilen alanlar güncellenir)

---

### S20: Düşük stok eşiği nasıl belirlenir?

**Cevap:**

**Düşük Stok Eşiği (Low Stock Threshold):** Stok miktarı bu değerin altına düştüğünde uyarı verilir.

**Belirleme Kriterleri:**
1. **Ürün Talep Hızı:**
   - Yüksek talep → Yüksek eşik (örn: 50)
   - Düşük talep → Düşük eşik (örn: 10)

2. **Tedarik Süresi:**
   - Uzun tedarik süresi → Yüksek eşik
   - Kısa tedarik süresi → Düşük eşik

3. **Ürün Değeri:**
   - Pahalı ürünler → Düşük eşik (fazla stok maliyeti)
   - Ucuz ürünler → Yüksek eşik (stoksuzluk riski)

**Varsayılan Değer:** 10

**Örnek:**
```
Laptop (Pahalı, Orta Talep, Uzun Tedarik):
- Eşik: 15

USB Kablo (Ucuz, Yüksek Talep, Kısa Tedarik):
- Eşik: 50

Ofis Sandalyesi (Pahalı, Düşük Talep, Uzun Tedarik):
- Eşik: 5
```

---

### S21: Ürün silindiğinde ne olur?

**Cevap:**

**Cascade Effects:**
1. **Stock Transactions:** İlgili stok işlemleri de silinir (CASCADE DELETE)
2. **Category:** Kategori etkilenmez (ürün sayısı azalır)
3. **Supplier:** Tedarikçi etkilenmez (ürün sayısı azalır)

**Önleyici Kontroller:**
- Admin onayı gerekir
- Silme işlemi geri alınamaz
- Confirmation dialog gösterilir

**Alternatif: Soft Delete (Future)**
Gelecek versiyonlarda:
```sql
ALTER TABLE products ADD COLUMN deleted BOOLEAN DEFAULT FALSE;
```
Ürünler fiziksel olarak silinmeyecek, sadece `deleted=true` işaretlenecek.

---

## Stok İşlemleri

### S22: Stok işlem tipleri nelerdir?

**Cevap:**

**1. PURCHASE (Alış):**
- **Amaç:** Tedarikçiden ürün alımı
- **Etki:** Stok miktarını artırır
- **Miktar:** Pozitif sayı
- **Örnek:** +50 adet laptop alındı

**2. SALE (Satış):**
- **Amaç:** Müşteriye ürün satışı
- **Etki:** Stok miktarını azaltır
- **Miktar:** Pozitif sayı (sistem otomatik eksi yapar)
- **Örnek:** -5 adet laptop satıldı
- **Kontrol:** Yeterli stok var mı?

**3. ADJUSTMENT (Düzeltme):**
- **Amaç:** Stok sayımı, fire, hasar düzeltmesi
- **Etki:** Artış veya azalış yapabilir
- **Miktar:** Pozitif (artış) veya negatif (azalış)
- **Örnek:** -3 adet hasarlı ürün düzeltmesi

---

### S23: Stok işlemi nasıl yapılır?

**Cevap:** (Sadece Admin)

**Frontend:**
1. Sol menüden "Transactions" sayfasına gidin
2. "New Transaction" butonuna tıklayın
3. Formu doldurun:
   - **Transaction Type:** PURCHASE, SALE veya ADJUSTMENT
   - **Product:** Dropdown'dan ürün seçin
   - **Quantity:** Miktar girin (pozitif sayı)
   - **Notes:** Açıklama ekleyin (isteğe bağlı)
4. "Record Transaction" butonuna tıklayın
5. Stok otomatik güncellenir

**API Örneği (Satış):**
```bash
POST /api/transactions
Authorization: Bearer <admin_token>
Content-Type: application/json

{
    "transactionType": "SALE",
    "productId": 1,
    "quantity": 5,
    "notes": "Customer order #12345"
}

Response:
{
    "id": 15,
    "transactionType": "SALE",
    "quantity": 5,
    "transactionDate": "2024-12-04T16:45:00",
    "product": {
        "id": 1,
        "name": "Laptop",
        "currentStock": 45  // 50 - 5 = 45
    },
    "user": {
        "username": "admin"
    }
}
```

---

### S24: Negatif stok olabilir mi?

**Cevap:**

**Mevcut Durum:** Hayır, negatif stok mümkün değildir.

**Kontroller:**
1. **Backend Validation:**
```java
if (transaction.getTransactionType() == TransactionType.SALE) {
    if (product.getQuantityInStock() < transaction.getQuantity()) {
        throw new InsufficientStockException(
            "Not enough stock. Available: " + product.getQuantityInStock()
        );
    }
}
```

2. **Frontend Validation:**
- Satış işleminde mevcut stok gösterilir
- Miktar girdisi max değer ile sınırlanır

**Hata Örneği:**
```json
{
    "status": 400,
    "message": "Insufficient stock. Available: 10, Requested: 15",
    "timestamp": "2024-12-04T17:00:00"
}
```

**Gelecek Özellik:** "Allow Backorder" seçeneği ile negatif stok izni verilebilir (gelecek versiyonda).

---

### S25: Stok işlemi düzeltilebilir mi?

**Cevap:**

**Mevcut Durum:** Hayır, kaydedilen işlemler düzenlenemez veya silinemez.

**Neden?**
- Audit trail (denetim izi) bozulur
- Muhasebe uyumsuzluğu olur
- Stok geçmişi tutarsız hale gelir

**Hatalı İşlem Nasıl Düzeltilir?**
1. **ADJUSTMENT** işlemi ile ters kayıt:
   ```
   Hata: 50 adet SALE kaydı (fazla satış)
   Düzeltme: +50 adet ADJUSTMENT (düzeltme artış)
   Sonuç: Stok eski haline döner
   ```

2. **Notes** alanına açıklama:
   ```
   "Düzeltme: 04-12-2024 tarihli #15 nolu satış işlemi iptal edildi"
   ```

**Gelecek Özellik:** "Void Transaction" (işlem iptali) özelliği eklenebilir.

---

### S26: Toplu stok işlemi yapılabilir mi?

**Cevap:**

**Mevcut Durum:** Hayır, işlemler tek tek kaydedilir.

**Workaround:**
Her ürün için ayrı işlem kaydı:
```bash
# Ürün 1
POST /api/transactions { "productId": 1, "quantity": 50, ... }

# Ürün 2
POST /api/transactions { "productId": 2, "quantity": 30, ... }

# Ürün 3
POST /api/transactions { "productId": 3, "quantity": 20, ... }
```

**Gelecek Özellik: Bulk Transaction API**
```bash
POST /api/transactions/bulk
{
    "transactionType": "PURCHASE",
    "transactions": [
        { "productId": 1, "quantity": 50 },
        { "productId": 2, "quantity": 30 },
        { "productId": 3, "quantity": 20 }
    ],
    "notes": "Bulk purchase from Supplier XYZ"
}
```

---

## Hata Çözümleri

### S27: "Cannot connect to database" hatası

**Cevap:**

**Olası Nedenler ve Çözümler:**

**1. MySQL Servisi Çalışmıyor**
```bash
# Linux
sudo systemctl status mysql
sudo systemctl start mysql

# macOS
brew services start mysql

# Windows
net start MySQL80
```

**2. Bağlantı Bilgileri Yanlış**
```properties
# application.properties kontrol edin
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_management_db
spring.datasource.username=root
spring.datasource.password=YOUR_CORRECT_PASSWORD
```

**3. Veritabanı Oluşturulmamış**
```sql
CREATE DATABASE inventory_management_db;
```

**4. Port Çakışması**
```bash
# MySQL portunu kontrol edin
netstat -an | grep 3306
# veya
lsof -i :3306
```

**5. Firewall Engeli**
```bash
# Linux - MySQL portunu aç
sudo ufw allow 3306
```

---

### S28: "401 Unauthorized" hatası

**Cevap:**

**Olası Nedenler:**

**1. Token Expire Olmuş**
- Access token 15 dakika sonra expire olur
- Çözüm: Otomatik refresh token mekanizması devreye girecektir
- Manuel çözüm: Logout/login yapın

**2. Token Yok veya Geçersiz**
```javascript
// Token kontrolü
const token = localStorage.getItem('accessToken');
if (!token) {
    // Redirect to login
    window.location.href = '/login';
}
```

**3. Token Formatı Yanlış**
```javascript
// Doğru format
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

// Yanlış
Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**4. JWT Secret Uyumsuz**
- Backend'de `jwt.secret` değiştirilmiş
- Çözüm: Logout/login ile yeni token alın

---

### S29: "403 Forbidden - Access Denied" hatası

**Cevap:**

**Neden:** Yetkisiz işlem denemesi (USER kullanıcısı admin işlemi yapmaya çalışıyor)

**Örnek:**
```
USER rolü ile:
- Ürün ekleme/düzenleme/silme
- Kategori yönetimi
- Tedarikçi yönetimi
- Stok işlemi yapma
```

**Çözüm:**
1. Admin hesabı ile giriş yapın
2. Veya admin'den yetki isteyin
3. Veya rolünüzü admin'e yükseltin (SQL ile)

**Backend Kontrolü:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/products")
public ResponseEntity<ProductResponse> createProduct(...) {
    // Only admin can access
}
```

---

### S30: "Duplicate entry" hatası

**Cevap:**

**Olası Durumlar:**

**1. Duplicate Username**
```
Error: Duplicate entry 'john' for key 'username'
Çözüm: Farklı kullanıcı adı seçin
```

**2. Duplicate SKU**
```
Error: Duplicate entry 'LAP-001' for key 'sku'
Çözüm: Benzersiz SKU kodu kullanın
```

**3. Duplicate Category Name**
```
Error: Duplicate entry 'Electronics' for key 'name'
Çözüm: Farklı kategori adı seçin
```

**4. Duplicate Supplier Email**
```
Error: Duplicate entry 'supplier@example.com' for key 'email'
Çözüm: Farklı e-posta adresi kullanın
```

**Benzersizlik Kontrolleri:**
- `users.username`
- `products.name`
- `products.sku`
- `categories.name`
- `suppliers.name`
- `suppliers.email`

---

### S31: "Token Refresh Failed" hatası

**Cevap:**

**Olası Nedenler:**

**1. Refresh Token Expire Olmuş**
- Refresh token 7 gün geçerli
- Çözüm: Yeniden giriş yapın

**2. Refresh Token Revoke Edilmiş**
- Logout yapıldığında token iptal edilir
- Çözüm: Login sayfasına yönlendir

**3. Refresh Token Bulunamadı**
```sql
-- Kontrol
SELECT * FROM refresh_tokens WHERE token = 'your-refresh-token';

-- Token yoksa yeniden login
```

**4. Database Hatası**
- Veritabanı bağlantısı kopmuş
- Çözüm: MySQL servisini kontrol edin

**Automatic Handling:**
```javascript
// ApiService.js
try {
    const newToken = await refreshToken();
} catch (error) {
    // Refresh failed - logout
    localStorage.clear();
    window.location.href = '/login';
}
```

---

## Performans ve Optimizasyon

### S32: Sistem yavaş çalışıyor, nasıl hızlandırırım?

**Cevap:**

**Backend Optimizasyonu:**

**1. Database Indexing**
```sql
-- Eksik index'leri ekleyin
CREATE INDEX idx_product_name ON products(name);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_transaction_date ON stock_transactions(transaction_date);
```

**2. Query Optimization**
```java
// N+1 problem çözümü
@Query("SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.supplier")
List<Product> findAllWithDetails();
```

**3. Connection Pooling**
```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

**4. Caching**
```java
@Cacheable("products")
public List<ProductResponse> getAllProducts() {
    // Cached for faster access
}
```

**Frontend Optimizasyonu:**

**1. Code Splitting**
```javascript
const ProductPage = lazy(() => import('./pages/ProductPage'));
```

**2. Memoization**
```javascript
const memoizedProducts = useMemo(() => 
    filterProducts(products, searchTerm),
    [products, searchTerm]
);
```

**3. Debouncing**
```javascript
const debouncedSearch = debounce((term) => {
    searchProducts(term);
}, 300);
```

---

### S33: Çok fazla ürün olunca sayfa donuyor

**Cevap:**

**Mevcut Problem:** Tüm ürünler tek seferde yükleniyor (100+ ürün → performans düşüşü)

**Çözüm 1: Pagination (Backend)**
```java
// ProductController.java
@GetMapping
public ResponseEntity<Page<ProductResponse>> getAllProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(productService.getAllProducts(pageable));
}
```

**Çözüm 2: Virtual Scrolling (Frontend)**
```javascript
import { FixedSizeList } from 'react-window';

<FixedSizeList
    height={600}
    itemCount={products.length}
    itemSize={50}
>
    {({ index, style }) => (
        <div style={style}>
            {products[index].name}
        </div>
    )}
</FixedSizeList>
```

**Çözüm 3: Lazy Loading**
- İlk 50 ürün yükle
- Scroll sonunda daha fazla yükle
- "Load More" butonu

---

## Deployment ve Production

### S34: Sistemi production'a nasıl alırım?

**Cevap:**

**Adım 1: Environment Variables**
```bash
# Production environment variables
export DB_HOST=production-db-host
export DB_PASSWORD=strong-production-password
export JWT_SECRET=very-strong-256-bit-secret-key
```

**Adım 2: application-prod.properties**
```properties
# Production configuration
spring.profiles.active=prod
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.root=WARN
server.port=8080
```

**Adım 3: Frontend Build**
```bash
cd frontend
npm run build
# build/ klasörü oluşur
```

**Adım 4: Backend JAR**
```bash
./gradlew clean build
# build/libs/stock-management-0.0.1.jar
```

**Adım 5: Deploy**
- AWS: Elastic Beanstalk
- Docker: Container deployment
- Traditional: JAR çalıştır

Detaylı deployment için `AWS_DEPLOYMENT_GUIDE.md` dökümanına bakınız.

---

### S35: HTTPS nasıl aktif edilir?

**Cevap:**

**Yöntem 1: Let's Encrypt (Free SSL)**
```bash
# Certbot ile SSL sertifikası al
sudo certbot --nginx -d yourdomain.com
```

**Yöntem 2: Spring Boot SSL**
```properties
# application.properties
server.port=8443
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

**Yöntem 3: Nginx Reverse Proxy**
```nginx
server {
    listen 443 ssl;
    server_name yourdomain.com;
    
    ssl_certificate /etc/ssl/certs/yourdomain.crt;
    ssl_certificate_key /etc/ssl/private/yourdomain.key;
    
    location / {
        proxy_pass http://localhost:8080;
    }
}
```

**Yöntem 4: AWS (CloudFront + ACM)**
- CloudFront distribution oluştur
- AWS Certificate Manager'dan SSL al
- Otomatik HTTPS

---

## AWS Cloud

### S36: AWS'de nasıl deploy edilir?

**Cevap:** Detaylı rehber için `AWS_DEPLOYMENT_GUIDE.md` dökümanına bakınız.

**Özet Adımlar:**

**1. RDS MySQL Database**
```
RDS → Create Database
Engine: MySQL 8.0.35
Instance: db.t3.micro
Storage: 20 GB SSD
Public Access: No
```

**2. Elastic Beanstalk (Backend)**
```bash
# EB CLI ile deploy
eb init -p java-21 stock-management
eb create production-env
eb deploy
```

**3. S3 + CloudFront (Frontend)**
```bash
# S3 bucket oluştur
aws s3 mb s3://stock-management-frontend

# Build'i upload et
aws s3 sync frontend/build/ s3://stock-management-frontend/

# CloudFront distribution oluştur
aws cloudfront create-distribution --origin-domain-name stock-management-frontend.s3.amazonaws.com
```

**4. Route 53 (DNS)**
- Domain kaydı
- CloudFront'a yönlendirme
- SSL sertifikası (ACM)

---

### S37: AWS maliyeti ne kadar?

**Cevap:**

**Tahmini Aylık Maliyet (Küçük Ölçek):**

| Servis | Konfigürasyon | Aylık Maliyet |
|--------|---------------|---------------|
| RDS MySQL | db.t3.micro, 20GB | ~$15 |
| Elastic Beanstalk | t3.micro EC2 | ~$10 |
| S3 | 1GB storage, 10K requests | ~$1 |
| CloudFront | 10GB data transfer | ~$1 |
| Route 53 | 1 hosted zone | ~$0.50 |
| **TOPLAM** | | **~$27.50/ay** |

**Free Tier (İlk 12 Ay):**
- RDS: 750 saat/ay db.t2.micro
- EC2: 750 saat/ay t2.micro
- S3: 5GB storage
- CloudFront: 50GB data transfer

**Maliyet Optimizasyonu:**
1. Reserved Instances kullan (1-3 yıllık)
2. Auto Scaling ile gereksiz kaynakları kapat
3. CloudWatch alarms ile monitoring
4. S3 lifecycle policies ile eski verileri archive et

---

### S38: AWS güvenlik ayarları nasıl yapılır?

**Cevap:**

**1. Security Groups**
```
RDS Security Group:
- Inbound: MySQL (3306) from EB Security Group only
- Outbound: All traffic

EB Security Group:
- Inbound: HTTP (80), HTTPS (443) from 0.0.0.0/0
- Outbound: All traffic
```

**2. IAM Roles**
```
Elastic Beanstalk Role:
- AWSElasticBeanstalkWebTier
- AWSElasticBeanstalkWorkerTier
- CloudWatchLogsFullAccess
```

**3. Environment Variables (Secrets)**
```bash
# Elastic Beanstalk environment properties
DB_HOST=mydb.xxxx.rds.amazonaws.com
DB_PASSWORD=encrypted-password
JWT_SECRET=encrypted-secret
```

**4. S3 Bucket Policy**
```json
{
    "Version": "2012-10-17",
    "Statement": [{
        "Sid": "PublicReadGetObject",
        "Effect": "Allow",
        "Principal": "*",
        "Action": "s3:GetObject",
        "Resource": "arn:aws:s3:::stock-management-frontend/*"
    }]
}
```

**5. CloudFront Security**
- HTTPS only
- AWS WAF integration
- Geo-restriction (optional)

---

## Ek Sorular

### S39: API rate limiting var mı?

**Cevap:**

**Mevcut Durum:** Hayır, rate limiting implementasyonu yok.

**Gelecek Özellik: Bucket4j ile Rate Limiting**
```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Bucket bucket = Bucket.builder()
        .addLimit(Bandwidth.simple(100, Duration.ofMinutes(1)))
        .build();
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, 
                             Object handler) {
        if (bucket.tryConsume(1)) {
            return true;
        }
        response.setStatus(429); // Too Many Requests
        return false;
    }
}
```

**Önerilen Limitler:**
- Authentication: 5 requests/minute
- Product CRUD: 100 requests/minute
- Dashboard: 20 requests/minute

---

### S40: Sistem loglama nasıl çalışıyor?

**Cevap:**

**Mevcut Loglama:**
```properties
# application.properties
logging.level.com.ims.stockmanagement=INFO
logging.level.org.springframework.security=DEBUG
logging.file.name=logs/stock-management.log
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

**Log Seviyeler:**
- **ERROR:** Kritik hatalar (database failure, authentication errors)
- **WARN:** Uyarılar (low stock, failed login attempts)
- **INFO:** Genel bilgiler (user login, product created)
- **DEBUG:** Detaylı bilgiler (SQL queries, JWT validation)

**Log Locations:**
```
Development: Console output
Production: logs/stock-management.log
AWS: CloudWatch Logs
```

**Örnek Loglar:**
```
2024-12-04 10:30:15 - User 'admin' logged in successfully
2024-12-04 10:31:22 - Product 'Laptop' created with SKU LAP-001
2024-12-04 10:32:10 - WARN: Low stock alert for product 'USB Cable' (qty: 5)
2024-12-04 10:33:45 - ERROR: Failed to delete product: Foreign key constraint
```

---

**FAQ Sonu**

**Son Güncelleme:** Aralık 2024  
**Versiyon:** 1.0  
**Hazırlayan:** Mehmet Taha Boynikoğlu (212 125 10 34)

---

**Ek Kaynaklar:**
- [User Manual](USER_MANUAL.md) - Kullanıcı kılavuzu
- [Technical Documentation](TECHNICAL_DOCUMENTATION.md) - Teknik dokümantasyon
- [AWS Deployment Guide](AWS_DEPLOYMENT_GUIDE.md) - AWS deployment rehberi
- [Project Presentation](PROJECT_PRESENTATION.md) - Proje sunumu
- [GitHub Repository](https://github.com/mrblackcoder/Stock_Management) - Kaynak kod
