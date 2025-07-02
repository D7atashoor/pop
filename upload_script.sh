#!/bin/bash

# سكريبت رفع مشروع IPTV Player
# Upload script for IPTV Player project

echo "🚀 سكريپت رفع مشروع IPTV Player"
echo "================================"

PROJECT_FILE="iptv-player-complete.zip"

if [ ! -f "$PROJECT_FILE" ]; then
    echo "❌ الملف غير موجود: $PROJECT_FILE"
    exit 1
fi

echo "📦 الملف موجود: $PROJECT_FILE"
echo "📏 حجم الملف: $(ls -lh $PROJECT_FILE | awk '{print $5}')"
echo ""

echo "🔗 خيارات الرفع المتاحة:"
echo "1. Google Drive (يدوي)"
echo "2. WeTransfer (يدوي)" 
echo "3. GitHub Release (باستخدام gh CLI)"
echo "4. إنشاء خادم محلي مؤقت"
echo ""

read -p "اختر الخيار (1-4): " choice

case $choice in
    1)
        echo "📁 Google Drive:"
        echo "1. اذهب إلى: https://drive.google.com"
        echo "2. ارفع الملف: $PROJECT_FILE" 
        echo "3. انقر بزر الماوس الأيمن > مشاركة"
        echo "4. اضبط على 'Anyone with the link'"
        echo "5. انسخ الرابط"
        ;;
    2)
        echo "📤 WeTransfer:"
        echo "1. اذهب إلى: https://wetransfer.com"
        echo "2. ارفع الملف: $PROJECT_FILE"
        echo "3. أدخل بريدك الإلكتروني"
        echo "4. احصل على رابط التحميل"
        ;;
    3)
        if command -v gh &> /dev/null; then
            echo "🐙 رفع على GitHub..."
            gh release create v1.0.0 "$PROJECT_FILE" \
                --title "IPTV Player v1.0.0" \
                --notes "مشروع IPTV Player كامل للأندرويد"
            echo "✅ تم الرفع بنجاح!"
        else
            echo "❌ GitHub CLI غير مثبت"
            echo "ثبته من: https://cli.github.com"
        fi
        ;;
    4)
        echo "🌐 إنشاء خادم محلي..."
        if command -v python3 &> /dev/null; then
            echo "🚀 الخادم يعمل على: http://localhost:8000"
            echo "📥 رابط التحميل: http://localhost:8000/$PROJECT_FILE"
            echo "⚠️  اضغط Ctrl+C للإيقاف"
            python3 -m http.server 8000
        else
            echo "❌ Python غير مثبت"
        fi
        ;;
    *)
        echo "❌ خيار غير صحيح"
        ;;
esac

echo ""
echo "✨ شكراً لاستخدام سكريپت الرفع!"