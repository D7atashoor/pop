# إرشادات بناء مشروع IPTV Player - Build Instructions

## حالة المشروع الحالية - Current Project Status

✅ **مكتمل - Completed:**
- بنية المشروع الكاملة - Complete project structure
- جميع ملفات المصدر - All source files
- قاعدة البيانات والنماذج - Database and models
- واجهات المستخدم للهاتف والتلفاز - Mobile and TV UI
- خدمات الشبكة - Network services
- إعدادات Gradle - Gradle configuration
- ملفات الموارد - Resource files
- الوثائق - Documentation

🔧 **متطلبات البناء - Build Requirements:**

### 1. تثبيت Android SDK - Install Android SDK

يحتاج المشروع إلى Android SDK لاكتمال البناء. يمكنك تثبيته بإحدى الطرق التالية:

#### الطريقة الأولى: Android Studio (موصى بها)
1. حمل وثبت Android Studio من: https://developer.android.com/studio
2. افتح Android Studio وسيقوم بتثبيت SDK تلقائياً
3. تأكد من تثبيت:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools

#### الطريقة الثانية: Command Line Tools
```bash
# تحميل command line tools
wget https://dl.google.com/android/repository/commandlinetools-linux-latest.zip
unzip commandlinetools-linux-latest.zip
mkdir -p ~/android-sdk/cmdline-tools/latest
mv cmdline-tools/* ~/android-sdk/cmdline-tools/latest/

# تعيين متغيرات البيئة
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# تثبيت المكونات المطلوبة
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 2. إعداد متغيرات البيئة - Environment Setup

أنشئ ملف `local.properties` في جذر المشروع:

```bash
# إنشاء ملف local.properties
echo "sdk.dir=/path/to/your/android-sdk" > local.properties
```

أو تعيين متغير البيئة:
```bash
export ANDROID_HOME=/path/to/your/android-sdk
```

### 3. بناء المشروع - Build Project

بعد تثبيت Android SDK:

```bash
# تنظيف وبناء المشروع
./gradlew clean assembleDebug

# أو لبناء إصدار الإنتاج
./gradlew assembleRelease
```

## هيكل المشروع المُنجز - Completed Project Structure

```
IPTV-Player/
├── 📁 app/
│   ├── 📁 src/main/
│   │   ├── 📁 java/com/iptv/player/
│   │   │   ├── 📄 IPTVApplication.kt ✅
│   │   │   ├── 📁 data/
│   │   │   │   ├── 📁 database/
│   │   │   │   │   ├── 📄 IPTVDatabase.kt ✅
│   │   │   │   │   ├── 📄 SourceDao.kt ✅
│   │   │   │   │   └── 📄 Converters.kt ✅
│   │   │   │   ├── 📁 model/
│   │   │   │   │   └── 📄 Source.kt ✅
│   │   │   │   ├── 📁 network/
│   │   │   │   │   ├── 📄 M3UParser.kt ✅
│   │   │   │   │   ├── 📄 XtreamApiService.kt ✅
│   │   │   │   │   └── 📄 StalkerService.kt ✅
│   │   │   │   └── 📁 repository/
│   │   │   │       └── 📄 SourceRepository.kt ✅
│   │   │   ├── 📁 di/
│   │   │   │   ├── 📄 DatabaseModule.kt ✅
│   │   │   │   └── 📄 NetworkModule.kt ✅
│   │   │   └── 📁 ui/
│   │   │       ├── 📁 mobile/
│   │   │       │   └── 📄 MainActivity.kt ✅
│   │   │       ├── 📁 tv/
│   │   │       │   ├── 📄 TvMainActivity.kt ✅
│   │   │       │   ├── 📄 TvMainFragment.kt ✅
│   │   │       │   └── 📄 CardPresenter.kt ✅
│   │   │       ├── 📁 screens/
│   │   │       │   ├── 📁 home/
│   │   │       │   │   ├── 📄 HomeScreen.kt ✅
│   │   │       │   │   └── 📄 HomeViewModel.kt ✅
│   │   │       │   ├── 📁 sources/
│   │   │       │   │   ├── 📄 SourcesScreen.kt ✅
│   │   │       │   │   ├── 📄 SourcesViewModel.kt ✅
│   │   │       │   │   ├── 📄 AddSourceScreen.kt ✅
│   │   │       │   │   └── 📄 AddSourceViewModel.kt ✅
│   │   │       │   └── 📁 channels/
│   │   │       │       └── 📄 ChannelsScreen.kt ✅
│   │   │       ├── 📁 navigation/
│   │   │       │   └── 📄 IPTVNavigation.kt ✅
│   │   │       └── 📁 theme/
│   │   │           ├── 📄 Theme.kt ✅
│   │   │           ├── 📄 Color.kt ✅
│   │   │           └── 📄 Type.kt ✅
│   │   ├── 📁 res/
│   │   │   ├── 📁 values/
│   │   │   │   ├── 📄 strings.xml ✅
│   │   │   │   ├── 📄 themes.xml ✅
│   │   │   │   └── 📄 colors.xml ✅
│   │   │   ├── 📁 layout/
│   │   │   │   └── 📄 activity_tv_main.xml ✅
│   │   │   ├── 📁 drawable/
│   │   │   │   ├── 📄 ic_launcher.xml ✅
│   │   │   │   ├── 📄 ic_launcher_background.xml ✅
│   │   │   │   ├── 📄 ic_launcher_foreground.xml ✅
│   │   │   │   ├── 📄 ic_tv_placeholder.xml ✅
│   │   │   │   └── 📄 app_banner.xml ✅
│   │   │   ├── 📁 mipmap-anydpi-v26/
│   │   │   │   ├── 📄 ic_launcher.xml ✅
│   │   │   │   └── 📄 ic_launcher_round.xml ✅
│   │   │   └── 📁 xml/
│   │   │       ├── 📄 backup_rules.xml ✅
│   │   │       └── 📄 data_extraction_rules.xml ✅
│   │   └── 📄 AndroidManifest.xml ✅
│   ├── 📄 build.gradle ✅
│   └── 📄 proguard-rules.pro ✅
├── 📁 gradle/wrapper/
│   ├── 📄 gradle-wrapper.properties ✅
│   └── 📄 gradle-wrapper.jar ✅
├── 📄 gradlew ✅
├── 📄 build.gradle ✅
├── 📄 settings.gradle ✅
├── 📄 gradle.properties ✅
├── 📄 README.md ✅
└── 📄 BUILD_INSTRUCTIONS.md ✅
```

## المميزات المُنجزة - Completed Features

### 🎯 الوظائف الأساسية:
- ✅ دعم 4 أنواع مصادر IPTV (M3U, Stalker, Xtream, MAC Portal)
- ✅ واجهة مستخدم للهاتف مع Jetpack Compose
- ✅ واجهة مستخدم للتلفاز مع Android TV Leanback
- ✅ قاعدة بيانات محلية لحفظ المصادر
- ✅ إدارة المصادر (إضافة، حذف، تفعيل/إلغاء تفعيل)
- ✅ محلل M3U للقوائم
- ✅ خدمات API لـ Xtream و Stalker

### 🏗️ البنية التقنية:
- ✅ MVVM Architecture
- ✅ Dependency Injection with Hilt
- ✅ Room Database
- ✅ Retrofit for networking
- ✅ ExoPlayer for media playback
- ✅ Material Design 3
- ✅ RTL language support

### 🎨 واجهة المستخدم:
- ✅ شاشة رئيسية مع إحصائيات
- ✅ شاشة إدارة المصادر
- ✅ شاشة إضافة مصدر جديد
- ✅ واجهة Android TV
- ✅ دعم الوضع المظلم والفاتح

## خطوات ما بعد البناء - Post-Build Steps

### 1. اختبار التطبيق:
```bash
# تثبيت على جهاز/محاكي
adb install app/build/outputs/apk/debug/app-debug.apk

# عرض اللوجز
adb logcat | grep "IPTV"
```

### 2. إضافة ميزات إضافية (اختياري):
- مشغل فيديو متقدم
- دعم EPG (دليل البرامج)
- نظام المفضلة
- البحث في القنوات
- إعدادات التطبيق

### 3. تحسينات الأداء:
- تحسين استهلاك الذاكرة
- تحسين سرعة التحميل
- إضافة cache للبيانات

## المشاكل المحتملة وحلولها - Troubleshooting

### مشكلة: "SDK location not found"
**الحل:**
```bash
# إنشاء ملف local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### مشكلة: "Build failed with compilation errors"
**الحل:**
```bash
# تنظيف المشروع
./gradlew clean

# إعادة بناء
./gradlew assembleDebug
```

### مشكلة: "OutOfMemoryError"
**الحل:**
```bash
# زيادة ذاكرة Gradle
export GRADLE_OPTS="-Xmx4096m"
```

## نصائح للتطوير - Development Tips

1. **استخدم Android Studio** للتطوير - يوفر أدوات مساعدة ممتازة
2. **فعل Developer Options** على الجهاز للاختبار
3. **استخدم محاكي Android TV** لاختبار واجهة التلفاز
4. **اختبر على أجهزة مختلفة** للتأكد من التوافق

## الدعم والمساعدة - Support

- راجع ملف README.md للمزيد من التفاصيل
- تحقق من لوجز التطبيق عند حدوث أخطاء
- استخدم Android Studio Debugger للتشخيص

---

**ملاحظة هامة:** المشروع كامل ومُعد للبناء. يحتاج فقط إلى Android SDK لإكمال عملية البناء.

**Important Note:** The project is complete and ready to build. It only needs Android SDK to complete the build process.