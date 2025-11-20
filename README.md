# Stock Management System

Web Tasarım ve Programlama dersi dönem projesi.

## Öğrenci Bilgileri
- **Ad Soyad:** Mehmet Taha Boynikoğlu
- **Öğrenci No:** 212 125 10 34
- **Proje Konusu:** Inventory Management System

## Proje Hakkında

Bu proje, bir web tabanlı envanter yönetim sistemidir. Kullanıcılar sisteme giriş yaparak ürünleri, kategorileri, tedarikçileri ve stok işlemlerini yönetebilirler.

### Özellikler
- Kullanıcı kayıt ve giriş sistemi
- Ürün yönetimi (ekleme, düzenleme, silme, listeleme)
- Kategori yönetimi
- Tedarikçi yönetimi
- Stok işlemleri takibi
- JWT tabanlı kimlik doğrulama
- External API entegrasyonu (döviz kuru)

## Teknolojiler

### Backend
- Java 25
- Spring Boot 3.5.7
- Spring Security
- Spring Data JPA
- MySQL 8.0
- JWT Authentication

### Frontend
- React 18
- React Router DOM
- Axios
- JavaScript ES6+

## Veritabanı

Proje 5 ana tabloya sahiptir:
1. **users** - Kullanıcı bilgileri
2. **products** - Ürün bilgileri
3. **categories** - Kategori bilgileri
4. **suppliers** - Tedarikçi bilgileri
5. **stock_transactions** - Stok işlemleri

## Kurulum ve Çalıştırma

### Gereksinimler
- Java 17+
- Node.js 16+
- MySQL 8.0+

### Backend Kurulum
```bash
# MySQL veritabanı oluştur
mysql -u root -p
CREATE DATABASE inventory_management_db;

# Projeyi çalıştır
./gradlew bootRun
```

Backend varsayılan olarak `http://localhost:8080` adresinde çalışır.

### Frontend Kurulum
```bash
cd frontend
npm install
npm start
```

Frontend varsayılan olarak `http://localhost:3000` adresinde çalışır.

## Kullanım

### Test Kullanıcıları
- **Admin:** username: `admin`, password: `admin123`
- **User:** username: `user`, password: `user123`

### API Endpoints
- `POST /api/auth/register` - Kullanıcı kaydı
- `POST /api/auth/login` - Giriş
- `GET /api/products` - Ürünleri listele
- `POST /api/products` - Ürün ekle (Admin)
- `GET /api/categories` - Kategorileri listele
- `GET /api/suppliers` - Tedarikçileri listele

## Proje Yapısı
```
src/
├── main/
│   ├── java/com/ims/stockmanagement/
│   │   ├── controllers/      # REST API endpoints
│   │   ├── services/         # İş mantığı
│   │   ├── repositories/     # Veritabanı erişimi
│   │   ├── entities/         # JPA entity sınıfları
│   │   ├── dtos/             # Data Transfer Objects
│   │   ├── security/         # JWT ve güvenlik
│   │   └── exceptions/       # Özel exception sınıfları
│   └── resources/
│       └── application.properties
└── frontend/
    └── src/
        ├── pages/            # React sayfaları
        ├── components/       # React bileşenleri
        └── service/          # API servisleri
```

## Notlar
- Proje Spring Boot 3.5.7 kullanmaktadır
- Frontend React 18 ile geliştirilmiştir
- MySQL 8.0 veritabanı kullanılmaktadır
- JWT ile kimlik doğrulama yapılmaktadır
