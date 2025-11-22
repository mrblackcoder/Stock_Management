#!/bin/bash

# Renkler
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

clear
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   📦 Inventory Management System - Start Script      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Proje dizini
PROJECT_DIR="/home/taha/IdeaProjects/StockManagement"

# 1. Port temizliği
echo -e "${YELLOW}[1/4]${NC} Portlar temizleniyor..."
sudo lsof -ti:8080 2>/dev/null | xargs -r sudo kill -9 2>/dev/null
sudo lsof -ti:3000 2>/dev/null | xargs -r sudo kill -9 2>/dev/null
pkill -9 -f "gradle" 2>/dev/null
pkill -9 -f "react-scripts" 2>/dev/null
sleep 1
echo -e "${GREEN}      ✓ Portlar temizlendi${NC}\n"

# 2. MySQL başlatma
echo -e "${YELLOW}[2/4]${NC} MySQL başlatılıyor..."
sudo service mysql start > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}      ✓ MySQL çalışıyor${NC}\n"
else
    echo -e "${RED}      ✗ MySQL başlatılamadı!${NC}"
    exit 1
fi

# 3. Backend başlatma
echo -e "${YELLOW}[3/4]${NC} Backend başlatılıyor..."
cd "$PROJECT_DIR"
./gradlew bootRun > backend.log 2>&1 &
BACKEND_PID=$!

# Backend'in hazır olmasını bekle (max 30 saniye)
for i in {1..30}; do
    if curl -s http://localhost:8080/api > /dev/null 2>&1; then
        echo -e "${GREEN}      ✓ Backend hazır (${i}s)${NC}\n"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}      ✗ Backend başlatılamadı! (Timeout)${NC}"
        echo -e "${YELLOW}      Log: tail -f backend.log${NC}"
        exit 1
    fi
    sleep 1
done

# 4. Frontend başlatma
echo -e "${YELLOW}[4/4]${NC} Frontend başlatılıyor..."
cd "$PROJECT_DIR/frontend"
npm start > /dev/null 2>&1 &
FRONTEND_PID=$!
sleep 2
echo -e "${GREEN}      ✓ Frontend başlatıldı${NC}\n"

# Başarı mesajı
echo -e "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║              ✓ Sistem Başarıyla Başlatıldı!          ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}📍 Erişim URL'leri:${NC}"
echo -e "   Backend  → http://localhost:8080"
echo -e "   Frontend → http://localhost:3000"
echo ""
echo -e "${BLUE}👤 Test Hesapları:${NC}"
echo -e "   Admin    → admin / admin123"
echo -e "   User     → user / user123"
echo ""
echo -e "${YELLOW}💡 Sistem durdurmak için:${NC}"
echo -e "   ./stop.sh veya Ctrl+C"
echo ""

