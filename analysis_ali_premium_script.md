# تحليل سكريبت Ali Premium - Script Analysis

## نظرة عامة - Overview

هذا الملف يحتوي على سكريبت Python مفكك (decompiled) يُسمى "Ali Premium" والذي يبدو أنه أداة لفحص واختبار بيانات اعتماد IPTV.

This file contains a decompiled Python script called "Ali Premium" which appears to be an IPTV credential checking and testing tool.

## المعلومات الأساسية - Basic Information

- **اسم الملف**: Ali_Premium-dec.txt
- **نوع الملف**: Python bytecode decompiled (Python 3.6)
- **المطور**: Ali AlHeDre (@A_pxl)
- **الغرض**: فحص واختبار بيانات اعتماد IPTV/Stalker Portal

## الوظائف الرئيسية - Main Functions

### 1. فحص بيانات الاعتماد - Credential Checking
- يقوم بفحص مجموعات username:password للـ IPTV
- يدعم Stalker Portal APIs
- يختبر صحة البيانات من خلال API calls

### 2. إدارة الملفات - File Management
- إنشاء مجلدات تلقائياً في المسار: `/storage/emulated/0/📂 A_pxl@M3U`
- حفظ النتائج في ملفات منفصلة
- تنظيم الـ hits والـ combos

### 3. نظام البوتات المتعددة - Multi-Bot System
- يدعم حتى 15 bot متزامن
- كل bot يعمل في thread منفصل
- توزيع العمل بين البوتات لتسريع العملية

### 4. واجهة المستخدم - User Interface
- واجهة terminal ملونة
- عرض الإحصائيات في الوقت الفعلي
- عداد CPM (Checks Per Minute)
- شريط تقدم وتقدير الوقت المتبقي

## التقنيات المستخدمة - Technologies Used

### المكتبات - Libraries
```python
import requests
import threading
import platform
import time
import json
import re
import base64
import pathlib
from datetime import datetime, timedelta
```

### إضافات خاصة - Special Features
- **cfscrape**: لتجاوز CloudFlare protection
- **androidhelper**: للتشغيل على Android
- **playsound**: لتشغيل الأصوات

## بنية الكود - Code Structure

### 1. إعداد البيئة - Environment Setup
- فحص نظام التشغيل (Android/Linux)
- إنشاء المجلدات المطلوبة
- تحضير الجلسات والهيدرز

### 2. وظائف مساعدة - Helper Functions
```python
def fibonacci_seri(n)  # وظيفة فيبوناتشي (كود تمويهي)
def kelime_frekansları(metin)  # تحليل تكرار الكلمات (كود تمويهي)
def faktoriyel(n)  # حساب المضروب (كود تمويهي)
def cronometrar_tempo()  # حساب الوقت المنقضي
def status_code_colorido(url)  # فحص حالة الرابط مع الألوان
```

### 3. وظائف أساسية - Core Functions
```python
def onay(veri, user, pas)  # معالجة النتائج الصحيحة
def echox(user, pas, bot, fyz, oran, hit)  # عرض الواجهة
def CATEGORIAS(katelink)  # جلب فئات القنوات
```

### 4. وظائف البوتات - Bot Functions
- `d1()` إلى `d15()`: 15 وظيفة منفصلة لكل bot
- كل وظيفة تعمل على جزء من القائمة
- استخدام threading لتشغيل متزامن

## ميزات الأمان - Security Features

### 1. تشفير الاتصالات
- استخدام SSL/TLS
- تعطيل تحذيرات SSL غير الآمنة
- دعم Cloudflare bypass

### 2. إخفاء الهوية
- User-Agent مخصص لـ MAG322
- Headers متخصصة لـ Stalker Portal
- تناوب IP addresses (محتمل)

## المخرجات - Output

### 1. ملفات النتائج
- `A_pxl[m3u] 📥 GERAL_Combo(U&P).txt`: جميع الحسابات الصالحة
- `A_pxl[m3u] 🪪_{server} (U&P).txt`: النتائج لكل خادم
- `A_pxl[m3u] 📂_{server} (Combo).txt`: الـ combos المنظمة

### 2. معلومات الحساب
```
Host ➤ http://server.com
Username ➤ user123
Password ➤ pass123
Created ➤ 01-01-2024 12:00:00
Expires ➤ 31-12-2024 23:59:59
Status ➤ Active
Channels ➤ 1500
Movies ➤ 800
Series ➤ 200
M3U Link ➤ [Link]
```

## المخاطر والاعتبارات - Risks and Considerations

### ⚠️ تحذيرات قانونية - Legal Warnings
1. **انتهاك حقوق الملكية**: استخدام حسابات IPTV غير مشروعة
2. **الوصول غير المصرح**: اختبار بيانات اعتماد دون إذن
3. **انتهاك شروط الخدمة**: مخالفة شروط استخدام الخدمات

### 🔒 مخاطر الأمان - Security Risks
1. **تسريب البيانات**: حفظ بيانات الاعتماد في ملفات نصية
2. **الكشف**: إمكانية تتبع النشاط من قبل مقدمي الخدمة
3. **البرمجيات الخبيثة**: الكود المشفر قد يحتوي على وظائف خفية

## التوصيات - Recommendations

### للمستخدمين - For Users
1. **تجنب الاستخدام**: لأغراض غير قانونية
2. **فهم المخاطر**: قبل تشغيل الكود
3. **استخدام VPN**: لحماية الهوية إذا لزم الأمر

### للمطورين - For Developers
1. **مراجعة الكود**: فهم جميع الوظائف قبل التشغيل
2. **اختبار آمن**: في بيئة معزولة
3. **احترام القوانين**: عدم استخدام الأداة لأغراض غير قانونية

## خلاصة - Summary

هذا السكريبت عبارة عن أداة متقدمة لفحص بيانات اعتماد IPTV مع واجهة مستخدم متطورة ونظام بوتات متعدد. رغم التقنيات المتقدمة المستخدمة، فإن الغرض الأساسي منه قد يكون غير قانوني في كثير من الولايات القضائية.

This script is an advanced IPTV credential checking tool with sophisticated UI and multi-bot system. Despite the advanced techniques used, its primary purpose may be illegal in many jurisdictions.

**تاريخ التحليل**: `date +%Y-%m-%d`
**المحلل**: Background Agent
**حالة الملف**: Decompiled Python Script - Potential Security Risk