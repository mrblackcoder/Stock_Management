#!/bin/bash

# Renkler
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

clear
echo -e "${YELLOW}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${YELLOW}║    🛑 Inventory Management System - Stop Script      ║${NC}"
echo -e "${YELLOW}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Backend'i durdur
echo -e "${YELLOW}[1/2]${NC} Backend durduruluyor..."
sudo pkill -9 -f "gradle" 2>/dev/null
sudo lsof -ti:8080 2>/dev/null | xargs -r sudo kill -9 2>/dev/null
echo -e "${GREEN}      ✓ Backend durduruldu${NC}\n"

# Frontend'i durdur
echo -e "${YELLOW}[2/2]${NC} Frontend durduruluyor..."
sudo pkill -9 -f "react-scripts" 2>/dev/null
sudo lsof -ti:3000 2>/dev/null | xargs -r sudo kill -9 2>/dev/null
echo -e "${GREEN}      ✓ Frontend durduruldu${NC}\n"

echo -e "${GREEN}✓ Sistem başarıyla durduruldu!${NC}"
echo ""

