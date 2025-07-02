# IPTV Player - مشغل IPTV

## Overview - نظرة عامة

IPTV Player is a modern Android application that supports both mobile phones and Android TV. The app provides hosting services for various IPTV sources without offering content itself.

مشغل IPTV هو تطبيق أندرويد حديث يدعم الهواتف المحمولة وأجهزة Android TV. يقدم التطبيق خدمات استضافة لمصادر IPTV المختلفة دون تقديم محتوى بحد ذاته.

## Features - المميزات

### Supported Source Types - أنواع المصادر المدعومة
- **M3U/M3U8 Playlists** - قوائم تشغيل M3U/M3U8
- **Stalker Portal** - بوابة Stalker
- **Xtream Codes API** - واجهة برمجة Xtream Codes
- **MAC Portal** - بوابة MAC

### Platform Support - دعم المنصات
- **Mobile Phones** - الهواتف المحمولة
- **Android TV** - أجهزة Android TV
- **Tablets** - الأجهزة اللوحية

### Key Features - المميزات الرئيسية
- Modern Material Design 3 UI - واجهة مستخدم عصرية
- RTL (Right-to-Left) language support - دعم اللغات من اليمين لليسار
- Dark/Light theme support - دعم الوضع المظلم والفاتح
- Local database for source management - قاعدة بيانات محلية لإدارة المصادر
- ExoPlayer integration for media playback - تكامل ExoPlayer لتشغيل الوسائط
- Dependency injection with Hilt - حقن التبعيات باستخدام Hilt

## Architecture - البنية المعمارية

The app follows modern Android development practices:

يتبع التطبيق ممارسات تطوير الأندرويد الحديثة:

- **MVVM Architecture** - معمارية MVVM
- **Jetpack Compose** for UI - لواجهة المستخدم
- **Room Database** for local storage - لقاعدة البيانات المحلية
- **Retrofit** for network operations - للعمليات الشبكية
- **Coroutines** for async operations - للعمليات غير المتزامنة
- **Navigation Component** - مكون التنقل
- **Hilt** for dependency injection - لحقن التبعيات

## Project Structure - هيكل المشروع

```
app/
├── src/main/java/com/iptv/player/
│   ├── data/
│   │   ├── database/          # Room database
│   │   ├── model/             # Data models
│   │   ├── network/           # API services & parsers
│   │   └── repository/        # Data repositories
│   ├── di/                    # Dependency injection modules
│   ├── ui/
│   │   ├── mobile/            # Mobile UI components
│   │   ├── tv/                # Android TV UI components
│   │   ├── screens/           # App screens
│   │   ├── navigation/        # Navigation setup
│   │   └── theme/             # UI theme & styling
│   └── IPTVApplication.kt     # Application class
└── src/main/res/              # Resources (layouts, strings, etc.)
```

## Getting Started - البدء

### Prerequisites - المتطلبات

- Android Studio Arctic Fox or later
- Android SDK 21+ (Android 5.0)
- Kotlin 1.9.0+

### Building the App - بناء التطبيق

1. Clone the repository - استنسخ المستودع
```bash
git clone <repository-url>
cd iptv-player
```

2. Open in Android Studio - افتح في Android Studio

3. Build and run - ابن وشغل التطبيق
```bash
./gradlew assembleDebug
```

### Configuration - التكوين

The app supports various IPTV source configurations:

يدعم التطبيق تكوينات مصادر IPTV مختلفة:

#### M3U Sources - مصادر M3U
- URL to M3U playlist - رابط قائمة تشغيل M3U
- Optional user agent and referer - وكيل المستخدم والمرجع (اختياري)

#### Stalker Portal - بوابة Stalker
- Portal URL - رابط البوابة
- MAC address - عنوان MAC
- Optional login credentials - بيانات تسجيل الدخول (اختياري)

#### Xtream Codes - أكواد Xtream
- Server URL - رابط الخادم
- Username and password - اسم المستخدم وكلمة المرور

#### MAC Portal - بوابة MAC
- Portal URL - رابط البوابة
- MAC address - عنوان MAC
- Optional serial number - الرقم التسلسلي (اختياري)

## Usage - الاستخدام

### Adding Sources - إضافة المصادر

1. Open the app - افتح التطبيق
2. Navigate to "Sources" - انتقل إلى "المصادر"
3. Tap the "+" button - اضغط على زر "+"
4. Select source type - اختر نوع المصدر
5. Fill in the required information - املأ المعلومات المطلوبة
6. Save the source - احفظ المصدر

### Managing Sources - إدارة المصادر

- Toggle sources on/off - تفعيل/إلغاء تفعيل المصادر
- Edit source details - تحرير تفاصيل المصدر
- Delete unwanted sources - حذف المصادر غير المرغوبة

## Technical Implementation - التنفيذ التقني

### Database Schema - مخطط قاعدة البيانات

The app uses Room database with the following entities:

يستخدم التطبيق قاعدة بيانات Room مع الكيانات التالية:

- **Sources** - المصادر
- **Channels** - القنوات (cached)
- **Movies** - الأفلام (cached)
- **Series** - المسلسلات (cached)

### Network Layer - طبقة الشبكة

- **M3UParser** - محلل M3U
- **XtreamApiService** - خدمة واجهة برمجة Xtream
- **StalkerApiService** - خدمة واجهة برمجة Stalker
- **MacPortalService** - خدمة بوابة MAC

### Media Playback - تشغيل الوسائط

Uses ExoPlayer for:
يستخدم ExoPlayer لـ:

- HLS streams - تدفقات HLS
- DASH streams - تدفقات DASH
- Direct URL streams - تدفقات الروابط المباشرة

## Contributing - المساهمة

Contributions are welcome! Please read our contributing guidelines.

المساهمات مرحب بها! يرجى قراءة إرشادات المساهمة.

## License - الترخيص

This project is licensed under the MIT License - see the LICENSE file for details.

هذا المشروع مرخص تحت رخصة MIT - راجع ملف LICENSE للتفاصيل.

## Disclaimer - إخلاء المسؤولية

This application is a media player and does not provide any content. Users are responsible for the content they access through their own sources.

هذا التطبيق مشغل وسائط ولا يقدم أي محتوى. المستخدمون مسؤولون عن المحتوى الذي يصلون إليه من خلال مصادرهم الخاصة.

## Support - الدعم

For support and questions, please open an issue in the repository.

للدعم والأسئلة، يرجى فتح مشكلة في المستودع.

---

**Note**: This is a basic IPTV player framework. Additional features like EPG support, favorites, search functionality, and advanced player controls can be added based on requirements.

**ملاحظة**: هذا إطار عمل أساسي لمشغل IPTV. يمكن إضافة ميزات إضافية مثل دعم EPG والمفضلة ووظائف البحث وعناصر التحكم المتقدمة في المشغل حسب المتطلبات.