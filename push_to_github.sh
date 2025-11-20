#!/bin/bash

echo "GitHub'a push yapiliyor..."
echo ""

cd /home/taha/IdeaProjects/StockManagement

# Remote kontrol et
if git remote | grep -q origin; then
    echo "Remote zaten var, guncelleniyor..."
    git remote set-url origin https://github.com/mrblackcoder/Stock_Management.git
else
    echo "Remote ekleniyor..."
    git remote add origin https://github.com/mrblackcoder/Stock_Management.git
fi

echo ""
echo "Git durumu:"
git status --short

echo ""
echo "Commit gecmisi:"
git log --oneline

echo ""
echo "Force push yapiliyor..."
git push -f origin main

echo ""
echo "Tamamlandi!"
echo ""
echo "GitHub'i kontrol et: https://github.com/mrblackcoder/Stock_Management"

