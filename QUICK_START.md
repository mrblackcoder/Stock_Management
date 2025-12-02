# 🚀 Hızlı Başlangıç Kılavuzu

## ⚡ Tek Komut ile Başlatma

### 1️⃣ MySQL'i Başlat
```bash
sudo service mysql start
```

### 2️⃣ Backend'i Başlat (Terminal 1)
```bash
cd Stock_Management
./gradlew bootRun
```

**Bekleme:** Backend'in tamamen başlamasını bekleyin (15-20 saniye).
**Başarı mesajı:** `Started StockManagementApplication`

### 3️⃣ Frontend'i Başlat (Terminal 2)
```bash
cd Stock_Management/frontend
npm start
```

**Bekleme:** Tarayıcı otomatik açılacak veya http://localhost:3000 adresine gidin.

---

## 📋 Test Kullanıcıları

### Admin Kullanıcı (Tam Yetki)
- **Kullanıcı Adı:** admin
- **Şifre:** admin123
- **Yetkiler:** Tüm CRUD işlemleri, ürün/kategori/tedarikçi yönetimi

### Normal Kullanıcı (Görüntüleme)
- **Kullanıcı Adı:** user
- **Şifre:** user123
- **Yetkiler:** Sadece görüntüleme

> **Not:** İlk kez kullanıyorsanız Register sayfasından yeni kullanıcı oluşturabilirsiniz.

---

## 🔄 Sorun Giderme

### Port 8080 veya 3000 kullanımda hatası
```bash
# Portları temizle
lsof -ti:8080 | xargs -r kill -9
lsof -ti:3000 | xargs -r kill -9
```

### Backend başlamıyor
```bash
# Gradle cache temizle
./gradlew clean

# Tekrar başlat
./gradlew bootRun
```

### MySQL bağlantı hatası
```bash
# MySQL şifresini application.properties'ten kontrol et
cat src/main/resources/application.properties | grep datasource

# Veritabanını manuel oluştur
mysql -u root -p
CREATE DATABASE IF NOT EXISTS inventory_management_db;
EXIT;
```

---

## 🌐 Erişim URL'leri

| Servis | URL | Durum |
|--------|-----|-------|
| **Frontend** | http://localhost:3000 | React UI |
| **Backend API** | http://localhost:8080/api | REST API |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API Docs |
| **Login** | http://localhost:3000/login | Giriş Sayfası |
| **Dashboard** | http://localhost:3000/dashboard | Ana Panel |

---

## 📊 API Endpoints

```bash
# API durumu kontrol
curl http://localhost:8080/api

# Kullanıcı login (JWT Token al)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Ürünleri listele (Token gerekli)
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## ✅ Sistem Kontrol

```bash
# Servislerin durumunu kontrol et
ps aux | grep -E "(java.*StockManagement|node.*react)"

# Logları kontrol et
tail -f /tmp/backend.log
tail -f /tmp/frontend.log
```

---

## 🎯 Özellikler

✅ JWT Authentication  
✅ Role-based Access Control (ADMIN/USER)  
✅ Product Management (CRUD)  
✅ Category Management  
✅ Supplier Management  
✅ Stock Transactions  
✅ Low Stock Alerts  
✅ Real-time Dashboard  
✅ External API Integration (Currency Conversion)  
✅ Responsive UI Design  
✅ **Admin Strategy Pattern** - Extensible admin operations (Bulk Delete, Price Update, Reports)  

---

## 🏗️ Mimari Özellikler

### Strategy Pattern Implementation
Proje, admin işlemleri için **Strategy Pattern** kullanır:

- `AdminOperationStrategy` - Base strategy interface
- `BulkDeleteStrategy` - Toplu silme işlemleri (ADMIN only)
- `BulkPriceUpdateStrategy` - Toplu fiyat güncellemeleri (ADMIN only)
- `ReportGenerationStrategy` - Rapor oluşturma (ALL users)
- `AdminOperationContext` - Strategy yönetimi ve yetkilendirme

**Örnek Kullanım:**
```java
@Autowired
private AdminOperationContext operationContext;

// ADMIN işlemi
operationContext.executeStrategy("BULK_DELETE", isAdmin);

// USER işlemi
operationContext.executeStrategy("REPORT_GENERATION", false);
```  

---

**Geliştirici:** Mehmet Taha Boynikoğlu  
**Öğrenci No:** 212 125 10 34  
**Proje:** Inventory Management System

