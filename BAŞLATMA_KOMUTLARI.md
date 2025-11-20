# 🚀 Proje Başlatma Komutları

## ✅ Doğru Başlatma Sırası

### 1️⃣ Backend'i Başlat (Port 8080)
```bash
cd /home/taha/IdeaProjects/StockManagement
./gradlew bootRun
```
**Beklenen Çıktı:**
```
Started StockManagementApplication in X seconds
Tomcat started on port 8080 (http)
```

### 2️⃣ Frontend'i Başlat (Port 3000)
**YENİ TERMINAL** açın ve:
```bash
cd /home/taha/IdeaProjects/StockManagement/frontend
npm start
```
**Beklenen Çıktı:**
```
Compiled successfully!
Local: http://localhost:3000
```

## 🌐 Erişim URL'leri

- **Frontend (React):** http://localhost:3000
- **Backend (Spring Boot):** http://localhost:8080
- **API Status:** http://localhost:8080/api

## 👤 Giriş Bilgileri

**Admin Kullanıcı:**
- Username: `admin`
- Password: `admin123`

**Normal Kullanıcı:**
- Username: `user`
- Password: `user123`

## 🔄 Process'leri Temizleme

Eğer portlar kullanımda hatası alırsanız:

```bash
# Backend process'lerini temizle
pkill -9 -f "gradle"
pkill -9 -f "java.*StockManagement"

# Frontend process'lerini temizle
pkill -9 -f "node.*react-scripts"

# Port kontrolü
lsof -i:8080  # Backend
lsof -i:3000  # Frontend
```

## 📊 Son Düzeltme: Recent Products Sıralaması

**Sorun:** Dashboard'da recent products eski ürünleri gösteriyordu

**Çözüm:** ✅ Düzeltildi
- Backend artık ürünleri **createdAt** tarihine göre **azalan** sırada (DESC) gönderiyor
- En son eklenen ürün **en üstte** görünüyor
- Değişiklikler `ProductController.java` ve `ProductService.java` dosyalarında yapıldı

**Test:**
1. Yeni bir ürün ekleyin
2. Dashboard'a gidin
3. "Recent Products" bölümünde yeni ürün **en üstte** gözükecek

## 🗄️ MySQL Veritabanı

Veritabanı otomatik oluşturulur:
- **Veritabanı Adı:** `inventory_management_db`
- **Host:** localhost:3306
- **Username:** root
- **Password:** root

MySQL çalışmıyorsa:
```bash
sudo systemctl start mysql
# veya
sudo service mysql start
```

## 🎯 Özellikler

✅ **Dashboard:** İstatistikler, son ürünler, düşük stok uyarıları  
✅ **Products:** CRUD işlemleri, stok takibi  
✅ **Categories:** Kategori yönetimi  
✅ **Suppliers:** Tedarikçi yönetimi  
✅ **Transactions:** Stok hareketleri (alış/satış)  
✅ **External API:** Döviz kuru entegrasyonu (ExchangeRate-API)  
✅ **Security:** JWT authentication, role-based access (ADMIN/USER)

## 📝 Notlar

- Backend başlamadan frontend başlatılırsa API istekleri 403/500 hatası verir
- İlk başlatmada backend otomatik demo veriler oluşturur
- Frontend hot-reload destekler, değişiklikler otomatik yansır

