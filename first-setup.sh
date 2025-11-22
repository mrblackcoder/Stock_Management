#!/bin/bash

# Renkler
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

clear
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     🔧 İLK KURULUM - First Time Setup                ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

PROJECT_DIR="/home/taha/IdeaProjects/StockManagement"

# 1. Port temizliği
echo -e "${YELLOW}[1/5]${NC} Portlar temizleniyor..."
sudo lsof -ti:8080 2>/dev/null | xargs -r sudo kill -9 2>/dev/null
sudo lsof -ti:3000 2>/dev/null | xargs -r sudo kill -9 2>/dev/null
pkill -9 -f "gradle" 2>/dev/null
pkill -9 -f "react-scripts" 2>/dev/null
echo -e "${GREEN}      ✓ Portlar temizlendi${NC}\n"

# 2. MySQL kurulum
echo -e "${YELLOW}[2/5]${NC} MySQL veritabanı oluşturuluyor..."
sudo service mysql start > /dev/null 2>&1
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS inventory_management_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}      ✓ Veritabanı hazır${NC}\n"
else
    echo -e "${RED}      ✗ Veritabanı oluşturulamadı!${NC}"
    echo -e "${YELLOW}      Manuel olarak oluşturun: mysql -u root -p${NC}"
    exit 1
fi

# 3. Backend build
echo -e "${YELLOW}[3/5]${NC} Backend build yapılıyor (bu 1-2 dakika sürebilir)..."
cd "$PROJECT_DIR"
./gradlew clean build -x test > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}      ✓ Backend build başarılı${NC}\n"
else
    echo -e "${RED}      ✗ Backend build hatası!${NC}"
    exit 1
fi

# 4. Frontend dependencies
echo -e "${YELLOW}[4/5]${NC} Frontend bağımlılıkları yükleniyor (bu 1-2 dakika sürebilir)..."
cd "$PROJECT_DIR/frontend"
npm install > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}      ✓ Frontend bağımlılıkları yüklendi${NC}\n"
else
    echo -e "${RED}      ✗ npm install hatası!${NC}"
    exit 1
fi

# 5. Script izinleri
echo -e "${YELLOW}[5/5]${NC} Script izinleri ayarlanıyor..."
cd "$PROJECT_DIR"
chmod +x start.sh stop.sh first-setup.sh
echo -e "${GREEN}      ✓ İzinler ayarlandı${NC}\n"

# Başarı mesajı
echo -e "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║           ✓ İlk Kurulum Başarıyla Tamamlandı!        ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}📍 Sistemi başlatmak için:${NC}"
echo -e "   ${GREEN}./start.sh${NC}"
echo ""
echo -e "${BLUE}📍 Sistemi durdurmak için:${NC}"
echo -e "   ${GREEN}./stop.sh${NC}"
echo ""
echo -e "${YELLOW}💡 Not: İlk kurulum tamamlandı. Bir daha çalıştırmanıza gerek yok.${NC}"
echo ""

