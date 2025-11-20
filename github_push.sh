#!/bin/bash
# GitHub Push Script - Stock Management Project
# Ogrenci: Mehmet Taha Boynikoglu (212 125 10 34)

echo "=========================================="
echo "  GitHub Push - Stock Management Project"
echo "=========================================="
echo ""

# Proje dizinine git
cd /home/taha/IdeaProjects/StockManagement || exit 1

echo "1. Git durumu kontrol ediliyor..."
if [ ! -d ".git" ]; then
    echo "   HATA: .git klasoru bulunamadi!"
    echo "   Lutfen once 'git init' yapin."
    exit 1
fi

echo "   ✓ Git repository var"
echo ""

echo "2. Remote URL ayarlaniyor..."
# Remote varsa guncelle, yoksa ekle
if git remote | grep -q "^origin$"; then
    git remote set-url origin https://github.com/mrblackcoder/Stock_Management.git
    echo "   ✓ Remote guncellendi"
else
    git remote add origin https://github.com/mrblackcoder/Stock_Management.git
    echo "   ✓ Remote eklendi"
fi
echo ""

echo "3. Git branch kontrol ediliyor..."
CURRENT_BRANCH=$(git branch --show-current)
if [ -z "$CURRENT_BRANCH" ]; then
    echo "   Branch olusturuluyor: main"
    git checkout -b main
fi
echo "   ✓ Branch: $CURRENT_BRANCH"
echo ""

echo "4. Commit sayisi:"
COMMIT_COUNT=$(git rev-list --count HEAD 2>/dev/null || echo "0")
echo "   $COMMIT_COUNT commit"
echo ""

echo "5. Son 5 commit:"
git log --oneline -5 2>/dev/null || echo "   Henuz commit yok"
echo ""

echo "6. GitHub'a push yapiliyor (force)..."
echo "   Repo: https://github.com/mrblackcoder/Stock_Management"
echo ""

# Force push
if git push -f origin main; then
    echo ""
    echo "=========================================="
    echo "  ✓ BASARIYLA TAMAMLANDI!"
    echo "=========================================="
    echo ""
    echo "GitHub'i kontrol et:"
    echo "https://github.com/mrblackcoder/Stock_Management"
    echo ""
else
    echo ""
    echo "=========================================="
    echo "  ✗ HATA OLUSTU!"
    echo "=========================================="
    echo ""
    echo "Olasi cozumler:"
    echo "1. GitHub username ve password kontrol et"
    echo "2. Personal Access Token kullan (sifre yerine)"
    echo "3. SSH key kullan: git@github.com:mrblackcoder/Stock_Management.git"
    echo ""
    exit 1
fi

