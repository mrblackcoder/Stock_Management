# 🧪 Test Kontrol Listesi

## Backend Testleri

### 1. Uygulama Başlatma
```bash
cd /home/taha/IdeaProjects/StockManagement
./gradlew bootRun
```
**Beklenen:** Backend 8080 portunda başarıyla başlamalı

### 2. Health Check
```bash
curl http://localhost:8080/actuator/health
```
**Beklenen:** `{"status":"UP"}` veya benzeri yanıt

### 3. Register API
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "test123",
    "role": "USER"
  }'
```
**Beklenen:** 200 OK ve JWT token

### 4. Login API
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }'
```
**Beklenen:** 200 OK ve JWT token

### 5. Categories API (Korumalı)
```bash
TOKEN="your-jwt-token-here"
curl http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN"
```
**Beklenen:** 200 OK ve kategori listesi

### 6. Products API
```bash
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"
```
**Beklenen:** 200 OK ve ürün listesi

---

## Frontend Testleri

### 1. Frontend Başlatma
```bash
cd /home/taha/IdeaProjects/StockManagement/frontend
npm start
```
**Beklenen:** Frontend 3000 portunda başlamalı

### 2. Login Sayfası
- URL: http://localhost:3000/login
- **Test:** Login formu görünmeli
- **Test:** Email ve şifre alanları çalışmalı

### 3. Register Sayfası
- URL: http://localhost:3000/register
- **Test:** Register formu görünmeli
- **Test:** Yeni kullanıcı kaydı yapılabilmeli

### 4. Dashboard
- URL: http://localhost:3000/dashboard
- **Test:** Login sonrası dashboard'a yönlendirme
- **Test:** Toplam istatistikler görünmeli
- **Test:** Navigation menüsü çalışmalı

### 5. Products Sayfası
- URL: http://localhost:3000/products
- **Test:** Ürün listesi yüklenmeli
- **Test:** Yeni ürün eklenebilmeli
- **Test:** Ürün düzenlenebilmeli
- **Test:** Ürün silinebilmeli

### 6. Categories Sayfası
- URL: http://localhost:3000/categories
- **Test:** Kategori listesi yüklenmeli
- **Test:** CRUD operasyonları çalışmalı

### 7. Suppliers Sayfası
- URL: http://localhost:3000/suppliers
- **Test:** Tedarikçi listesi yüklenmeli
- **Test:** CRUD operasyonları çalışmalı

---

## Database Testleri

### 1. MySQL Bağlantısı
```bash
mysql -u root -p
USE inventory_management_db;
SHOW TABLES;
```
**Beklenen:** 5 tablo görünmeli:
- users
- products
- categories
- suppliers
- stock_transactions

### 2. Veri Kontrolü
```sql
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM products;
SELECT COUNT(*) FROM categories;
SELECT COUNT(*) FROM suppliers;
SELECT COUNT(*) FROM stock_transactions;
```

---

## Güvenlik Testleri

### 1. Yetkisiz Erişim (401)
```bash
curl http://localhost:8080/api/products
```
**Beklenen:** 401 Unauthorized veya 403 Forbidden

### 2. Geçersiz Token (403)
```bash
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer invalid-token"
```
**Beklenen:** 403 Forbidden

### 3. CORS Kontrolü
- Frontend'den backend'e istek atılabilmeli
- CORS hatası olmamalı

---

## Build Testleri

### 1. Backend Build
```bash
./gradlew clean build -x test
```
**Beklenen:** BUILD SUCCESSFUL

### 2. Frontend Build
```bash
cd frontend
npm run build
```
**Beklenen:** Build klasörü oluşmalı

### 3. JAR Dosyası
```bash
java -jar build/libs/StockManagement-0.0.1-SNAPSHOT.jar
```
**Beklenen:** Uygulama başarıyla çalışmalı

---

## Dokümantasyon Kontrolleri

- [x] README.md mevcut ve güncel
- [x] API_DOCUMENTATION.md mevcut
- [x] DEPLOYMENT_GUIDE.md mevcut
- [x] PROJE_OZETI.md mevcut
- [x] Tüm endpoint'ler dokümante edilmiş
- [x] Kurulum adımları açık
- [x] Troubleshooting bölümü var

---

## Git Kontrolleri

```bash
# Commit sayısı kontrolü
git log --oneline | wc -l

# Son commit'leri görme
git log --oneline -10

# Remote kontrolü
git remote -v

# Branch kontrolü
git branch -a
```

**Beklenen:**
- En az 5 anlamlı commit
- Düzenli commit geçmişi
- Origin remote tanımlı
- main branch mevcut

---

## Son Kontroller

### Gereksinim Karşılama Kontrolü

| Gereksinim | Durum | Kanıt |
|------------|-------|-------|
| 5+ Tablo | ✅ | users, products, categories, suppliers, stock_transactions |
| İlişkisel DB | ✅ | Foreign key'ler mevcut |
| Login/Register | ✅ | JWT authentication çalışıyor |
| CRUD | ✅ | Tüm tablolarda var |
| External API | ✅ | ExternalApiService.java |
| Remote Access | ✅ | REST API endpoints |
| Embedded UI | ✅ | Thymeleaf templates |
| React SPA | ✅ | frontend/ klasörü |
| Spring Security | ✅ | SecurityConfig.java |
| MySQL | ✅ | application.properties |

---

## Performans Testleri (Opsiyonel)

### 1. Response Time
```bash
time curl http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"
```
**Beklenen:** < 500ms

### 2. Concurrent Requests
```bash
for i in {1..10}; do
  curl http://localhost:8080/api/products \
    -H "Authorization: Bearer $TOKEN" &
done
wait
```
**Beklenen:** Tüm istekler başarılı

---

## Hata Durumları

### Test Edilmesi Gerekenler:
- [ ] Geçersiz email formatı
- [ ] Zayıf şifre
- [ ] Duplicate username
- [ ] Boş form gönderme
- [ ] Negatif stok değeri
- [ ] Olmayan ID ile işlem
- [ ] Token süresi dolmuş
- [ ] Database bağlantısı kesilmiş

---

## Test Sonuç Raporu

### Tarih: _______________
### Test Eden: _______________

| Test Kategorisi | Toplam | Başarılı | Başarısız | Not |
|----------------|--------|----------|-----------|-----|
| Backend API | | | | |
| Frontend UI | | | | |
| Database | | | | |
| Security | | | | |
| Build | | | | |
| Documentation | | | | |
| **TOPLAM** | | | | |

### Kritik Hatalar:
- 

### Minör Hatalar:
- 

### İyileştirme Önerileri:
- 

---

**Test Tamamlanma Durumu:** ⬜ Tamamlandı / ⬜ Devam Ediyor

**Proje Teslime Hazır mı?** ⬜ Evet / ⬜ Hayır

**Notlar:**

