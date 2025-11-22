#!/bin/bash

echo "🚀 Inventory Management System - Başlatma Scripti"
echo "=================================================="
echo ""

# Renkler
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Eski process'leri temizle
echo -e "${YELLOW}⏳ Eski process'ler temizleniyor...${NC}"
sudo lsof -ti:8080 | xargs -r sudo kill -9 2>/dev/null
sudo lsof -ti:3000 | xargs -r sudo kill -9 2>/dev/null
pkill -9 -f "gradle" 2>/dev/null
pkill -9 -f "react-scripts" 2>/dev/null
sleep 2
echo -e "${GREEN}✅ Portlar temizlendi${NC}"
echo ""

# 2. MySQL'i başlat
echo -e "${YELLOW}⏳ MySQL başlatılıyor...${NC}"
sudo service mysql start
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ MySQL başlatıldı${NC}"
else
    echo -e "${RED}❌ MySQL başlatılamadı!${NC}"
    exit 1
fi
echo ""

# 3. Backend'i başlat
echo -e "${YELLOW}⏳ Backend (Spring Boot) başlatılıyor...${NC}"
cd /home/taha/IdeaProjects/StockManagement
./gradlew bootRun > backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}✅ Backend başlatıldı (PID: $BACKEND_PID)${NC}"
echo "   Loglar: backend.log"
echo ""

# 4. Backend'in hazır olmasını bekle
echo -e "${YELLOW}⏳ Backend'in hazır olması bekleniyor (20 saniye)...${NC}"
for i in {20..1}; do
    echo -ne "\r   $i saniye kaldı..."
    sleep 1
done
echo -e "\n${GREEN}✅ Backend hazır olmalı${NC}"
echo ""

# 5. Backend kontrolü
echo -e "${YELLOW}⏳ Backend kontrol ediliyor...${NC}"
RESPONSE=$(curl -s http://localhost:8080/api)
if [ -n "$RESPONSE" ]; then
    echo -e "${GREEN}✅ Backend çalışıyor!${NC}"
    echo "   URL: http://localhost:8080"
else
    echo -e "${RED}❌ Backend yanıt vermiyor. backend.log dosyasını kontrol edin.${NC}"
    echo ""
    echo "Son 10 satır backend log:"
    tail -10 backend.log
    exit 1
fi
echo ""

# 6. Frontend'i başlat
echo -e "${YELLOW}⏳ Frontend (React) başlatılıyor...${NC}"
cd /home/taha/IdeaProjects/StockManagement/frontend
npm start &
FRONTEND_PID=$!
echo -e "${GREEN}✅ Frontend başlatıldı (PID: $FRONTEND_PID)${NC}"
echo ""

# Bilgi mesajları
echo "=================================================="
echo -e "${GREEN}✅ Sistem başarıyla başlatıldı!${NC}"
echo ""
echo "📌 Erişim Bilgileri:"
echo "   Backend:  http://localhost:8080"
echo "   Frontend: http://localhost:3000"
echo ""
echo "📌 Test Kullanıcıları:"
echo "   Admin: admin / admin123"
echo "   User:  user / user123"
echo ""
echo "📌 Sistemi Durdurmak İçin:"
echo "   Ctrl+C veya:"
echo "   sudo pkill -9 -f 'gradle'"
echo "   sudo pkill -9 -f 'react-scripts'"
echo "=================================================="

