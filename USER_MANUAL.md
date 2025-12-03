# User Manual
## Stock Management System - Kullanıcı Kılavuzu

**Proje:** Inventory Management System  
**Öğrenci:** Mehmet Taha Boynikoğlu (212 125 10 34)  
**Versiyon:** 1.0  
**Tarih:** Aralık 2024

---

## İçindekiler

1. [Giriş](#giriş)
2. [Sistem Gereksinimleri](#sistem-gereksinimleri)
3. [Kurulum ve Başlangıç](#kurulum-ve-başlangıç)
4. [Kullanıcı Arayüzü](#kullanıcı-arayüzü)
5. [Temel İşlevler](#temel-işlevler)
6. [Admin İşlevleri](#admin-işlevleri)
7. [Sık Karşılaşılan Sorunlar](#sık-karşılaşılan-sorunlar)
8. [Güvenlik ve Gizlilik](#güvenlik-ve-gizlilik)

---

## Giriş

### Sistem Hakkında

Stock Management System (Envanter Yönetim Sistemi), işletmelerin ürün stoklarını, kategorilerini, tedarikçilerini ve işlemlerini dijital ortamda yönetmelerine olanak sağlayan kapsamlı bir web uygulamasıdır.

### Temel Özellikler

- **Ürün Yönetimi:** Ürün ekleme, düzenleme, silme ve arama
- **Kategori Yönetimi:** Ürünleri kategorilere ayırma
- **Tedarikçi Yönetimi:** Tedarikçi bilgilerini saklama ve yönetme
- **Stok İşlemleri:** Alış, satış ve düzeltme işlemlerini kaydetme
- **Düşük Stok Uyarıları:** Kritik stok seviyelerinde otomatik bildirimler
- **Dashboard:** Gerçek zamanlı stok durumu ve istatistikler
- **Rol Tabanlı Erişim:** Admin ve normal kullanıcı yetkileri

### Kullanıcı Rolleri

**ADMIN (Yönetici):**
- Tüm sistemde tam yetki
- Ürün, kategori, tedarikçi ekleme/düzenleme/silme
- Stok işlemleri yapma
- Tüm raporları görüntüleme
- Kullanıcı yönetimi

**USER (Kullanıcı):**
- Ürünleri görüntüleme ve arama
- Dashboard istatistiklerini görüntüleme
- Düşük stok uyarılarını görüntüleme
- Profil bilgilerini güncelleme

---

## Sistem Gereksinimleri

### Minimum Gereksinimler

**Donanım:**
- İşlemci: Dual-core 2.0 GHz
- RAM: 4 GB
- Disk Alanı: 500 MB

**Yazılım:**
- İşletim Sistemi: Windows 10/11, macOS 10.15+, Linux (Ubuntu 20.04+)
- Web Tarayıcı: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- Java: JDK 21+
- MySQL: 8.0+
- Node.js: 18+ (sadece geliştirme için)

### Önerilen Gereksinimler

**Donanım:**
- İşlemci: Quad-core 2.5 GHz+
- RAM: 8 GB+
- Disk Alanı: 2 GB+
- İnternet Bağlantısı: 5 Mbps+

**Yazılım:**
- En güncel tarayıcı sürümleri
- SSD disk kullanımı
- Düzenli yedekleme sistemi

---

## Kurulum ve Başlangıç

### İlk Kurulum

1. **Sistemi İndirin**
   ```bash
   git clone https://github.com/mrblackcoder/Stock_Management.git
   cd Stock_Management
   ```

2. **Veritabanını Oluşturun**
   ```bash
   mysql -u root -p
   CREATE DATABASE inventory_management_db;
   exit;
   ```

3. **Uygulamayı Başlatın**
   ```bash
   # Backend
   ./gradlew bootRun
   
   # Frontend (yeni terminal)
   cd frontend
   npm install
   npm start
   ```

4. **Uygulamaya Erişin**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Swagger Dokümantasyonu: http://localhost:8080/swagger-ui.html

### İlk Giriş

**Varsayılan Admin Hesabı:**
- Kullanıcı Adı: `admin`
- Şifre: `Admin@123!Secure`

**Önemli:** İlk girişten sonra şifrenizi mutlaka değiştirin!

### Yeni Kullanıcı Kaydı

1. Giriş sayfasında "Register" butonuna tıklayın
2. Gerekli bilgileri doldurun:
   - Kullanıcı Adı (3-20 karakter)
   - Şifre (min 8 karakter, büyük-küçük harf, rakam, özel karakter)
   - Ad Soyad
3. "Register" butonuna tıklayın
4. Otomatik olarak USER rolü ile sisteme kaydolursunuz

---

## Kullanıcı Arayüzü

### Ana Sayfa (Dashboard)

Dashboard, sistemin genel durumunu gösterir:

**Üst Kısım - İstatistikler:**
- **Toplam Ürün:** Sistemdeki toplam ürün sayısı
- **Düşük Stok:** Stok seviyesi düşük ürün sayısı
- **Son İşlemler:** Yapılan işlem sayısı

**Orta Kısım - Son Eklenen Ürünler:**
- En son eklenen 6 ürün
- Ürün adı, kategori, stok durumu
- Detaylı görüntüleme butonu

**Alt Kısım - Düşük Stok Uyarıları:**
- Kritik ve düşük stok seviyesindeki ürünler
- Kırmızı: Kritik seviye (stok < 10)
- Turuncu: Düşük seviye (stok < düşük_stok_eşiği)
- Yeniden sipariş butonu (Admin)

### Navigasyon Menüsü

Sol taraftaki menüden erişilebilir sayfalar:

**Tüm Kullanıcılar:**
- **Dashboard:** Ana sayfa
- **Products:** Ürün listesi ve arama
- **Profile:** Kullanıcı profili

**Sadece Admin:**
- **Categories:** Kategori yönetimi
- **Suppliers:** Tedarikçi yönetimi
- **Transactions:** Stok işlemleri

### Üst Bar

- **Sistem Başlığı:** "Inventory Management System"
- **Kullanıcı Profili:** Kullanıcı adı ve ilk harf avatarı
- **Rol Göstergesi:** Mevcut rol (ADMIN/USER)
- **Logout Butonu:** Sistemden çıkış

---

## Temel İşlevler

### 1. Ürün Görüntüleme ve Arama

**Ürün Listesi:**
1. Sol menüden "Products" tıklayın
2. Tüm ürünler tablo halinde görüntülenir
3. Gösterilen bilgiler:
   - Ürün Adı
   - SKU (Stok Kodu)
   - Kategori
   - Tedarikçi
   - Fiyat
   - Stok Miktarı
   - Durum (Stokta/Düşük/Tükendi)

**Arama Fonksiyonu:**
1. Arama kutusuna ürün adı veya SKU yazın
2. Sistem otomatik olarak eşleşen ürünleri filtreler
3. Arama sonuçları anında güncellenir

**Kategori Filtreleme:**
1. "Filter by Category" açılır menüsünden kategori seçin
2. Sadece seçili kategorideki ürünler görüntülenir
3. "All Categories" seçeneği tüm ürünleri gösterir

**Ürün Detayları:**
1. Ürün satırındaki "Details" butonuna tıklayın
2. Ürün detay sayfası açılır:
   - Tam ürün bilgileri
   - Stok geçmişi
   - İlgili işlemler
   - Düzenleme ve silme butonları (Admin)

### 2. Dashboard Kullanımı

**İstatistikleri Anlama:**

- **Toplam Ürün Kartı:**
  - Mavi renk
  - Sistemdeki toplam ürün sayısı
  - Tıklanabilir, Products sayfasına yönlendirir

- **Düşük Stok Kartı:**
  - Turuncu renk
  - Düşük stok seviyesindeki ürün sayısı
  - Kritik durum uyarısı içerir

- **İşlem Kartı:**
  - Yeşil renk
  - Toplam stok işlemi sayısı
  - Alış, satış, düzeltme işlemlerini kapsar

**Son Ürünler Bölümü:**
- En son eklenen 6 ürün gösterilir
- Her ürün için:
  - Ürün adı
  - Kategori
  - Stok durumu renk kodu
  - "View Details" butonu

**Düşük Stok Uyarıları:**
- Kritik ve düşük stok seviyesindeki tüm ürünler
- Renk kodları:
  - Kırmızı: Kritik (stok < 10)
  - Turuncu: Düşük (stok < eşik)
- Admin kullanıcılar "Reorder" butonu ile hızlı sipariş verebilir

### 3. Profil Yönetimi

**Profil Bilgilerini Görüntüleme:**
1. Sol menüden "Profile" tıklayın
2. Görüntülenen bilgiler:
   - Kullanıcı Adı
   - Tam Ad
   - Rol (ADMIN/USER)
   - Hesap Oluşturma Tarihi
   - Son Güncelleme Tarihi

**Profil Bilgilerini Güncelleme:**
1. "Edit Profile" butonuna tıklayın
2. Güncellenebilir alanlar:
   - Tam Ad
   - Yeni Şifre (isteğe bağlı)
3. "Save Changes" butonuna tıklayın
4. Başarılı mesajı görüntülenir

**Şifre Değiştirme:**
1. Profil düzenleme modunda
2. "New Password" alanına yeni şifre girin
3. Şifre gereksinimleri:
   - Minimum 8 karakter
   - En az 1 büyük harf
   - En az 1 küçük harf
   - En az 1 rakam
   - En az 1 özel karakter
4. "Save Changes" ile kaydedin

---

## Admin İşlevleri

### 1. Ürün Yönetimi

**Yeni Ürün Ekleme:**
1. Products sayfasında "Add New Product" butonuna tıklayın
2. Formu doldurun:
   - **Ürün Adı:** Benzersiz ürün ismi (zorunlu)
   - **SKU:** Stok Kodu (zorunlu, benzersiz)
   - **Açıklama:** Ürün detayları (isteğe bağlı)
   - **Kategori:** Açılır menüden seçin (zorunlu)
   - **Tedarikçi:** Açılır menüden seçin (zorunlu)
   - **Fiyat:** Birim fiyat (zorunlu, pozitif sayı)
   - **Stok Miktarı:** Başlangıç stoğu (zorunlu, 0 veya pozitif)
   - **Düşük Stok Eşiği:** Uyarı seviyesi (zorunlu, pozitif sayı)
3. "Create Product" butonuna tıklayın
4. Başarılı mesajı görüntülenir ve ürün listeye eklenir

**Ürün Düzenleme:**
1. Ürün satırında "Edit" butonuna tıklayın
2. Mevcut bilgiler formda görüntülenir
3. Değişiklik yapın
4. "Update Product" butonuna tıklayın
5. Değişiklikler kaydedilir

**Ürün Silme:**
1. Ürün satırında "Delete" butonuna tıklayın
2. Onay mesajı görüntülenir
3. "Yes, Delete" ile onaylayın
4. Ürün ve ilgili işlemler sistemden silinir
5. **Dikkat:** Bu işlem geri alınamaz!

**Toplu İşlemler:**
1. "Bulk Actions" menüsünden işlem seçin:
   - **Bulk Delete:** Çoklu ürün silme
   - **Update Prices:** Toplu fiyat güncelleme
   - **Export Report:** Ürün raporunu dışa aktarma
2. Ürünleri seçin (checkbox)
3. İşlemi uygulayın

### 2. Kategori Yönetimi

**Kategori Listesi:**
1. Sol menüden "Categories" tıklayın
2. Tüm kategoriler tablo halinde görüntülenir:
   - Kategori Adı
   - Açıklama
   - Ürün Sayısı
   - Oluşturma Tarihi

**Yeni Kategori Ekleme:**
1. "Add New Category" butonuna tıklayın
2. Formu doldurun:
   - **Kategori Adı:** Benzersiz isim (zorunlu)
   - **Açıklama:** Kategori detayları (isteğe bağlı)
3. "Create Category" butonuna tıklayın

**Kategori Düzenleme:**
1. Kategori satırında "Edit" butonuna tıklayın
2. Bilgileri güncelleyin
3. "Update Category" ile kaydedin

**Kategori Silme:**
1. Kategori satırında "Delete" butonuna tıklayın
2. **Dikkat:** Kategoriye ait ürünler varsa önce ürünlerin kategorisini değiştirin
3. Onay ile silme işlemini tamamlayın

### 3. Tedarikçi Yönetimi

**Tedarikçi Listesi:**
1. Sol menüden "Suppliers" tıklayın
2. Görüntülenen bilgiler:
   - Tedarikçi Adı
   - İletişim Kişisi
   - E-posta
   - Telefon
   - Adres
   - Ürün Sayısı

**Yeni Tedarikçi Ekleme:**
1. "Add New Supplier" butonuna tıklayın
2. Formu doldurun:
   - **Tedarikçi Adı:** Firma adı (zorunlu)
   - **İletişim Kişisi:** Yetkili kişi adı (zorunlu)
   - **E-posta:** Geçerli e-posta adresi (zorunlu)
   - **Telefon:** İletişim telefonu (zorunlu)
   - **Adres:** Firma adresi (isteğe bağlı)
3. "Create Supplier" butonuna tıklayın

**Tedarikçi Düzenleme:**
1. Tedarikçi satırında "Edit" butonuna tıklayın
2. Bilgileri güncelleyin
3. "Update Supplier" ile kaydedin

**Tedarikçi Silme:**
1. Tedarikçi satırında "Delete" butonuna tıklayın
2. **Dikkat:** Tedarikçiye ait ürünler varsa önce ürünlerin tedarikçisini değiştirin
3. Onay ile silme işlemini tamamlayın

**Tedarikçi Ürünlerini Görüntüleme:**
1. Tedarikçi satırında "View Products" butonuna tıklayın
2. Tedarikçiye ait tüm ürünler modal pencerede görüntülenir
3. Ürün detaylarına ulaşılabilir

### 4. Stok İşlemleri

**İşlem Tipleri:**

1. **PURCHASE (Alış):**
   - Stoka ürün ekleme
   - Stok miktarını artırır
   - Tedarikçiden alım kaydı

2. **SALE (Satış):**
   - Stoktan ürün çıkışı
   - Stok miktarını azaltır
   - Müşteriye satış kaydı

3. **ADJUSTMENT (Düzeltme):**
   - Stok sayımı sonrası düzeltme
   - Artış veya azalış yapılabilir
   - Fire, hasar, kayıp düzeltmeleri

**Yeni İşlem Oluşturma:**
1. Sol menüden "Transactions" tıklayın
2. "New Transaction" butonuna tıklayın
3. Formu doldurun:
   - **İşlem Tipi:** PURCHASE, SALE veya ADJUSTMENT seçin
   - **Ürün:** Açılır menüden ürün seçin
   - **Miktar:** İşlem miktarı (pozitif sayı)
   - **Not:** Açıklama veya referans (isteğe bağlı)
4. "Record Transaction" butonuna tıklayın
5. Stok otomatik olarak güncellenir

**İşlem Geçmişi:**
1. Transactions sayfasında tüm işlemler listelenir:
   - İşlem Tarihi ve Saati
   - İşlem Tipi (renkli badge)
   - Ürün Adı
   - Miktar
   - Güncel Stok
   - İşlemi Yapan Kullanıcı
   - Notlar
2. Filtreleme:
   - İşlem tipine göre
   - Tarihe göre
   - Ürüne göre
3. Sıralama:
   - En yeni işlemler üstte

**İşlem Raporu:**
1. "Export Report" butonuna tıklayın
2. Tarih aralığı seçin
3. Excel veya PDF formatında rapor alın
4. Rapor içeriği:
   - Tüm işlemler
   - Toplam alış/satış miktarları
   - Stok değişim grafiği

### 5. Raporlama ve Analiz

**Dashboard Raporları:**
- Gerçek zamanlı stok durumu
- Düşük stok uyarıları
- Son işlem özeti
- Hızlı istatistikler

**Ürün Raporları:**
- En çok satılan ürünler
- Düşük stoktaki ürünler
- Kategori bazlı dağılım
- Tedarikçi bazlı dağılım

**İşlem Raporları:**
- Günlük/haftalık/aylık işlem özeti
- Kullanıcı bazlı işlem geçmişi
- Ürün bazlı işlem detayları

**Dışa Aktarma:**
- Excel formatında
- PDF formatında
- JSON formatında (API entegrasyonu için)

---

## Sık Karşılaşılan Sorunlar

### Giriş Sorunları

**Problem:** "Geçersiz kullanıcı adı veya şifre" hatası

**Çözüm:**
1. Kullanıcı adı ve şifrenizi kontrol edin (büyük-küçük harf duyarlı)
2. Caps Lock tuşunun kapalı olduğundan emin olun
3. Şifrenizi unuttuysanız admin ile iletişime geçin
4. Tarayıcı çerezlerini ve önbelleğini temizleyin

**Problem:** "Hesabınız kilitlendi" mesajı

**Çözüm:**
1. 5 başarısız giriş denemesinden sonra hesap 15 dakika kilitlenir
2. 15 dakika bekleyin veya admin ile iletişime geçin
3. Admin, hesabın kilidini manuel olarak açabilir

### Ürün İşlemleri

**Problem:** Ürün eklenemiyor

**Çözüm:**
1. Tüm zorunlu alanların doldurulduğunu kontrol edin
2. SKU kodunun benzersiz olduğundan emin olun
3. Fiyat ve stok değerlerinin pozitif sayı olduğunu kontrol edin
4. Kategori ve tedarikçi seçildiğinden emin olun
5. İnternet bağlantınızı kontrol edin

**Problem:** Ürün silinemiy or

**Çözüm:**
1. Admin yetkisine sahip olduğunuzdan emin olun
2. Ürüne ait işlemler varsa önce işlemleri kontrol edin
3. Tarayıcıyı yenileyin ve tekrar deneyin
4. Hata devam ederse admin/geliştirici ile iletişime geçin

### Stok İşlemleri

**Problem:** Stok işlemi kaydedilmiyor

**Çözüm:**
1. İşlem miktarının pozitif bir sayı olduğunu kontrol edin
2. SALE işleminde yeterli stok olduğundan emin olun
3. Ürün seçiminin yapıldığını kontrol edin
4. Admin yetkisine sahip olduğunuzdan emin olun

**Problem:** Stok miktarı yanlış görünüyor

**Çözüm:**
1. Sayfayı yenileyin (F5)
2. İşlem geçmişini kontrol ederek son işlemleri inceleyin
3. ADJUSTMENT işlemi ile stok düzeltmesi yapın
4. Veritabanı tutarsızlığı varsa teknik destek alın

### Performans Sorunları

**Problem:** Sayfa yavaş yükleniyor

**Çözüm:**
1. İnternet bağlantınızı kontrol edin
2. Tarayıcı önbelleğini temizleyin
3. Gereksiz sekmeler/uygulamaları kapatın
4. Farklı bir tarayıcı deneyin
5. Sistemde çok fazla veri varsa filtreleme kullanın

**Problem:** Arama çok yavaş

**Çözüm:**
1. Arama terimini daha spesifik yapın
2. Kategori filtresi kullanarak sonuçları daraltın
3. Sayfalama kullanarak daha az veri görüntüleyin

### Görüntü Sorunları

**Problem:** Sayfa düzgün görünmüyor

**Çözüm:**
1. Tarayıcıyı tam ekran yapın
2. Zoom seviyesini %100 olarak ayarlayın (Ctrl+0)
3. Tarayıcıyı güncelleyin
4. JavaScript ve CSS'in etkin olduğundan emin olun
5. Farklı bir tarayıcı deneyin (Chrome önerilir)

---

## Güvenlik ve Gizlilik

### Şifre Politikası

**Güçlü Şifre Oluşturma:**
- Minimum 8 karakter
- En az 1 büyük harf (A-Z)
- En az 1 küçük harf (a-z)
- En az 1 rakam (0-9)
- En az 1 özel karakter (!@#$%^&*)
- Kişisel bilgiler kullanmayın
- Düzenli olarak değiştirin (3 ayda bir)

**Şifre Güvenliği:**
- Şifrenizi kimseyle paylaşmayın
- Birden fazla sistemde aynı şifreyi kullanmayın
- Şifrenizi tarayıcıya kaydetmekten kaçının
- Güvenli şifre yöneticisi kullanın

### Oturum Yönetimi

**Güvenli Oturum:**
- Her oturum JWT token ile korunur
- Access token 15 dakika geçerlidir
- Refresh token 7 gün geçerlidir
- Otomatik token yenileme aktif
- Logout yapıldığında tüm tokenlar iptal edilir

**Güvenlik İpuçları:**
- Paylaşımlı bilgisayarlarda her zaman logout yapın
- "Beni Hatırla" seçeneğini dikkatli kullanın
- İşiniz bitince tarayıcıyı kapatın
- Şüpheli aktivite görürseniz hemen logout yapın

### Veri Güvenliği

**Şifreleme:**
- Tüm şifreler BCrypt ile hash'lenir
- API iletişimi JWT token ile güvenlidir
- Hassas veriler client-side şifrelenir
- HTTPS kullanımı önerilir (production)

**Yedekleme:**
- Otomatik günlük veritabanı yedekleri
- 7 gün yedek saklama süresi
- Manuel yedek alma özelliği
- Yedekler güvenli konumda saklanır

**Veri Gizliliği:**
- Kullanıcı verileri güvenle saklanır
- Üçüncü taraflarla paylaşılmaz
- KVKK uyumlu veri işleme
- Veri silme taleplerine uyulur

### Erişim Kontrolü

**Rol Tabanlı Yetkilendirme:**
- Her işlem için yetki kontrolü
- Admin-only işlevler korunur
- Unauthorized erişim engellenir
- Audit log kaydı tutulur

**API Güvenliği:**
- Her istek JWT token ile doğrulanır
- Rate limiting uygulanır
- CORS politikası aktif
- SQL injection koruması
- XSS koruma mekanizmaları

### Güvenlik En İyi Uygulamaları

**Kullanıcı Tarafında:**
1. Güçlü ve benzersiz şifre kullanın
2. İki faktörlü doğrulama aktif olduğunda kullanın
3. Güncel tarayıcı ve işletim sistemi kullanın
4. Antivirüs yazılımı güncel tutun
5. Şüpheli bağlantılara tıklamayın
6. Düzenli olarak aktivite loglarınızı kontrol edin

**Yönetici Tarafında:**
1. Düzenli güvenlik güncellemeleri yapın
2. Veritabanı yedeklerini kontrol edin
3. Kullanıcı aktivitelerini izleyin
4. Şüpheli davranışları tespit edin
5. Güvenlik loglarını inceleyin
6. Firewall ve güvenlik duvarlarını aktif tutun

---

## Destek ve İletişim

### Teknik Destek

**Problem Bildirme:**
1. Hatayı detaylı açıklayın
2. Ekran görüntüsü ekleyin
3. Hangi sayfada olduğunuzu belirtin
4. Tarayıcı ve işletim sistemi bilgisi verin
5. Yaptığınız işlemleri sıralayın

**İletişim:**
- **Geliştirici:** Mehmet Taha Boynikoğlu
- **Öğrenci No:** 212 125 10 34
- **GitHub:** https://github.com/mrblackcoder/Stock_Management

### Eğitim ve Dokümantasyon

**Mevcut Kaynaklar:**
- Bu Kullanıcı Kılavuzu
- Teknik Dokümantasyon
- API Dokümantasyonu (Swagger)
- Video Eğitim Materyalleri (varsa)
- Sık Sorulan Sorular (FAQ)

### Güncellemeler

**Versiyon Takibi:**
- Mevcut versiyon: 1.0.0
- Son güncelleme: Aralık 2024
- Yeni özellikler GitHub'da duyurulur
- Kritik güvenlik güncellemeleri hemen uygulanır

---

## Ek Bilgiler

### Klavye Kısayolları

- **Ctrl + K:** Arama kutusuna odaklan
- **Ctrl + N:** Yeni ürün ekle (Admin)
- **Ctrl + H:** Ana sayfaya dön
- **Ctrl + L:** Logout
- **F5:** Sayfayı yenile
- **Esc:** Modal/popup kapat

### Mobil Kullanım

Sistem responsive tasarıma sahiptir:
- Telefon ve tabletlerde çalışır
- Dokunmatik kontroller desteklenir
- Menü otomatik küçülür
- Tablolar kaydırılabilir
- Mobil tarayıcılar desteklenir

### Tarayıcı Desteği

**Tam Destek:**
- Google Chrome 90+
- Mozilla Firefox 88+
- Microsoft Edge 90+
- Safari 14+

**Sınırlı Destek:**
- Internet Explorer 11 (desteklenmez)
- Eski tarayıcı sürümleri

---

**Son Güncelleme:** Aralık 2024  
**Versiyon:** 1.0  
**Hazırlayan:** Mehmet Taha Boynikoğlu

**Kullanıcı Kılavuzu Sonu**
