# 🎯 IPTV Unified API - دليل التكامل الشامل

## 📋 نظرة عامة

تم استخراج وتطوير API موحد شامل لخدمات IPTV من تحليل عدة ملفات متخصصة، يوفر واجهة برمجة تطبيقات متكاملة تدعم جميع البروتوكولات الشائعة للتكامل مع **تطبيق Android IPTV Player**.

## 🔍 الملفات المحللة والمستخرجة

### 📁 المصادر الأساسية:
1. **ViagraUltra_Portal_Check_2024.txt** - فاحص بوابات متقدم (45+ نوع بوابة)
2. **A_pxll313_√ᵖʳᵒ³_Flag-dec.txt** - فاحص شامل متعدد البروتوكولات 
3. **Ali_Premium-dec.txt** - فاحص IPTV/Stalker Portal (سابق)

### 🎯 ما تم استخراجه:
- **45+ Portal Types** مختلفة
- **MAC Address Generation** متقدم
- **Token-based Authentication** 
- **Geographic Location Detection**
- **M3U Parsing** شامل
- **Batch Processing** للفحص المتعدد
- **Real-time Portal Discovery**

---

## 🏗️ البنية التقنية

### 📦 المكونات الرئيسية:

```
├── unified_iptv_api.py          # Core API Library
├── flask_iptv_api_server.py     # REST API Server
├── android_integration_example.kt  # Android Integration
├── comprehensive_iptv_api_analysis.md  # Technical Analysis
└── requirements_api.txt         # Dependencies
```

### 🔧 التقنيات المستخدمة:
- **Python 3.6+** - اللغة الأساسية
- **Flask** - REST API Framework
- **Requests** - HTTP Client Library
- **Threading** - المعالجة المتوازية
- **Hashlib** - التشفير وتوليد المعرفات
- **RegEx** - تحليل البيانات والنصوص

---

## 🚀 التشغيل السريع

### 1️⃣ تثبيت المتطلبات:
```bash
pip install -r requirements_api.txt
```

### 2️⃣ تشغيل الخادم:
```bash
python flask_iptv_api_server.py
```

### 3️⃣ اختبار API:
```bash
curl http://localhost:5000/
```

---

## 📚 واجهات API المتاحة

### 🔍 1. Portal Discovery - اكتشاف البوابات
**POST** `/api/discover`

```json
{
  "host": "example.com:8080"
}
```

**Response:**
```json
{
  "success": true,
  "status_code": 200,
  "endpoint": "/stalker_portal/server/load.php",
  "portal_type": "stalker",
  "host": "example.com:8080",
  "full_url": "http://example.com:8080/stalker_portal/server/load.php",
  "geo_info": {
    "ip": "1.2.3.4",
    "country": "United States",
    "country_code": "US",
    "city": "New York",
    "isp": "Example ISP"
  }
}
```

### 🎭 2. Stalker Portal Check - فحص ستالكر بورتال
**POST** `/api/stalker/check`

```json
{
  "host": "example.com:8080",
  "mac": "00:1A:79:01:CA:35",
  "portal_path": "/stalker_portal/server/load.php",
  "include_channels": true,
  "include_vod": true,
  "include_series": true
}
```

**Response:**
```json
{
  "success": true,
  "host": "example.com:8080",
  "mac": "00:1A:79:01:CA:35",
  "token": "abc123token",
  "profile": {
    "id": "12345",
    "login": "user123",
    "status": "active"
  },
  "account_info": {
    "phone": "Dec 31, 2024",
    "full_name": "Test User"
  },
  "channels": [...],
  "channels_count": 150
}
```

### 📺 3. Xtream Codes Check - فحص إكستريم كودز
**POST** `/api/xtream/check`

```json
{
  "host": "example.com:8080",
  "username": "test_user",
  "password": "test_pass",
  "include_content": true
}
```

**Response:**
```json
{
  "success": true,
  "host": "example.com:8080",
  "username": "test_user",
  "user_info": {
    "username": "test_user",
    "status": "Active",
    "exp_date": "1735689599",
    "max_connections": "2"
  },
  "server_info": {
    "url": "example.com",
    "port": "8080",
    "timezone": "Europe/London"
  },
  "m3u_url": "http://example.com:8080/get.php?username=test_user&password=test_pass&type=m3u_plus",
  "xmltv_url": "http://example.com:8080/xmltv.php?username=test_user&password=test_pass"
}
```

### 📄 4. M3U Parser - تحليل M3U
**POST** `/api/m3u/parse`

```json
{
  "url": "http://example.com/playlist.m3u"
}
```
أو
```json
{
  "content": "#EXTM3U\n#EXTINF:-1,Channel Name\nhttp://stream.url"
}
```

**Response:**
```json
{
  "success": true,
  "channels_count": 100,
  "channels": [
    {
      "name": "Channel Name",
      "url": "http://stream.url",
      "tvg-id": "channel1",
      "tvg-logo": "http://logo.url",
      "group-title": "Sports"
    }
  ],
  "categories": {
    "Sports": 25,
    "News": 30,
    "Movies": 45
  }
}
```

### 🎲 5. MAC Address Generation - توليد عناوين MAC
**POST** `/api/utils/generate_mac`

```json
{
  "prefix": "00:1A:79:",
  "count": 5
}
```

**Response:**
```json
{
  "success": true,
  "count": 5,
  "macs": [
    {
      "mac": "00:1A:79:01:CA:35",
      "device_credentials": {
        "mac": "00:1A:79:01:CA:35",
        "mac_encoded": "00%3A1A%3A79%3A01%3ACA%3A35",
        "serial_number": "ABC123DEF4567",
        "device_id": "1234567890ABCDEF",
        "signature": "FEDCBA0987654321",
        "stb_type": "MAG254"
      }
    }
  ]
}
```

### 🌍 6. Geographic Location - الموقع الجغرافي
**POST** `/api/utils/geo_location`

```json
{
  "ip_or_host": "example.com"
}
```

**Response:**
```json
{
  "success": true,
  "geo_info": {
    "ip": "1.2.3.4",
    "country": "United States",
    "country_code": "US",
    "country_flag": "🇺🇸",
    "city": "New York",
    "region": "New York",
    "isp": "Example ISP",
    "continent": "North America"
  }
}
```

### 📊 7. Batch Check - الفحص المتعدد
**POST** `/api/batch/check`

```json
{
  "type": "stalker",
  "accounts": [
    {
      "host": "server1.com:8080",
      "mac": "00:1A:79:01:CA:35"
    },
    {
      "host": "server2.com:8080", 
      "mac": "00:1A:79:01:CA:36"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "task_id": "task_1",
  "message": "Batch check started",
  "total_accounts": 2
}
```

### 📈 8. Task Status - حالة المهمة
**GET** `/api/task/{task_id}`

**Response:**
```json
{
  "success": true,
  "task": {
    "id": "task_1",
    "type": "batch_check",
    "status": "completed",
    "progress": 100,
    "total": 2,
    "results": [...]
  }
}
```

---

## 📱 التكامل مع Android

### 🔧 إعداد Dependencies في `build.gradle`:

```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

### 💻 مثال الاستخدام في Android:

```kotlin
// إنشاء مثيل من API Service
val apiService = IPTVApiService("http://your-api-server:5000")

// اكتشاف البوابة
lifecycleScope.launch {
    val result = apiService.discoverPortal("example.com:8080")
    if (result.success) {
        val portalInfo = result.data
        // استخدام معلومات البوابة
    }
}

// فحص Stalker Portal
lifecycleScope.launch {
    val result = apiService.checkStalker(
        host = "example.com:8080",
        mac = "00:1A:79:01:CA:35",
        includeChannels = true
    )
    
    if (result.success) {
        val stalkerData = result.data
        // حفظ البيانات في قاعدة البيانات المحلية
    }
}

// توليد MAC عشوائي
lifecycleScope.launch {
    val result = apiService.generateMAC(count = 1)
    if (result.success && result.data?.isNotEmpty() == true) {
        val generatedMAC = result.data.first().mac
        // استخدام MAC المولد
    }
}
```

### 🏗️ التكامل مع Architecture الموجود:

```kotlin
// في AddSourceViewModel
class AddSourceViewModel(
    private val sourceRepository: SourceRepository,
    private val iptvApiService: IPTVApiService
) : ViewModel() {
    
    fun addStalkerSource(name: String, host: String, mac: String) {
        viewModelScope.launch {
            try {
                // اكتشاف البوابة أولاً
                val discoveryResult = iptvApiService.discoverPortal(host)
                
                // فحص الاتصال
                val checkResult = iptvApiService.checkStalker(host, mac)
                
                if (checkResult.success) {
                    // إنشاء مصدر جديد
                    val source = Source(
                        name = name,
                        type = SourceType.STALKER_PORTAL,
                        host = host,
                        mac = mac,
                        isActive = true
                    )
                    
                    // حفظ في قاعدة البيانات
                    sourceRepository.insertSource(source)
                }
            } catch (e: Exception) {
                // معالجة الخطأ
            }
        }
    }
}
```

---

## 🎯 الميزات المتقدمة

### 🚀 Portal Types المدعومة (45+ نوع):

```python
PORTAL_TYPES = [
    '/portal.php',                    # Standard MAG Portal
    '/server/load.php',               # Stalker Load
    '/stalker_portal/server/load.php', # Full Stalker
    '/c/portal.php',                  # NXT Portal
    '/ministra/portal.php',           # Ministra
    '/magaccess/portal.php',          # MAG Access
    '/bs.mag.portal.php',             # BS MAG
    '/magportal/portal.php',          # MAG Portal
    '/tek/server/load.php',           # TEK Portal
    '/emu/server/load.php',           # EMU Portal
    '/portalmega.php',                # Mega Portal
    '/rmxportal/portal.php',          # RMX Portal
    '/powerfull/portal.php',          # PowerFull Portal
    '/nettvmag/portal.php',           # NetTV MAG
    '/cmdforex/portal.php',           # CMD Forex
    '/extraportal.php',               # Extra Portal
    '/delko/portal.php',              # Delko Portal
    '/bStream/portal.php',            # BStream Portal
    '/blowportal/portal.php',         # Blow Portal
    # ... و المزيد
]
```

### 🎲 MAC Prefixes المدعومة:

```python
MAC_PREFIXES = [
    '00:1A:79:',  # Standard MAG
    '78:A3:52:',  # Alternative 1
    '10:27:BE:',  # Alternative 2
    'A0:BB:3E:',  # Alternative 3
    'D0:9F:D9:',  # Alternative 4
    '04:D6:AA:',  # Alternative 5
    # ... و المزيد
]
```

### 🌍 Geographic Detection Features:

- **IP Geolocation** - تحديد موقع الخادم
- **Country Flags** - أعلام الدول
- **ISP Detection** - تحديد مزود الخدمة
- **Timezone Info** - معلومات المنطقة الزمنية
- **VPN Detection** - كشف استخدام VPN

---

## 🔧 الاستخدامات المتقدمة

### 1️⃣ فحص متعدد بـ Threading:

```python
import threading
from concurrent.futures import ThreadPoolExecutor

def batch_check_accounts(accounts, max_workers=10):
    with ThreadPoolExecutor(max_workers=max_workers) as executor:
        futures = []
        
        for account in accounts:
            future = executor.submit(check_single_account, account)
            futures.append(future)
        
        results = []
        for future in futures:
            try:
                result = future.result(timeout=30)
                results.append(result)
            except Exception as e:
                results.append({'error': str(e)})
        
        return results
```

### 2️⃣ MAC Generation مع Custom Logic:

```python
def generate_sequential_macs(prefix, count, start_suffix=None):
    """توليد عناوين MAC متسلسلة"""
    macs = []
    
    if start_suffix:
        current = int(start_suffix.replace(':', ''), 16)
    else:
        current = 0
    
    for i in range(count):
        suffix = f"{current:06X}"
        formatted_suffix = f"{suffix[:2]}:{suffix[2:4]}:{suffix[4:6]}"
        mac = f"{prefix}{formatted_suffix}"
        macs.append(mac)
        current += 1
    
    return macs
```

### 3️⃣ Smart Portal Detection:

```python
def smart_portal_detection(host):
    """اكتشاف ذكي للبوابة مع تحليل الاستجابة"""
    
    # فحص أنواع مختلفة
    portal_tests = [
        ('/stalker_portal/server/load.php', 'stalker'),
        ('/portal.php', 'mag'),
        ('/c/portal.php', 'nxt'),
        ('/ministra/portal.php', 'ministra')
    ]
    
    results = []
    
    for endpoint, portal_type in portal_tests:
        try:
            response = requests.get(f"http://{host}{endpoint}", timeout=5)
            
            # تحليل محتوى الاستجابة
            content_analysis = analyze_response_content(response.text)
            
            results.append({
                'endpoint': endpoint,
                'type': portal_type,
                'status_code': response.status_code,
                'analysis': content_analysis,
                'confidence': calculate_confidence_score(response, portal_type)
            })
            
        except Exception as e:
            continue
    
    # ترتيب النتائج حسب الثقة
    results.sort(key=lambda x: x['confidence'], reverse=True)
    
    return results[0] if results else None
```

---

## 📈 مراقبة الأداء والإحصائيات

### 📊 Metrics Collection:

```python
class APIMetrics:
    def __init__(self):
        self.requests_count = 0
        self.successful_checks = 0
        self.failed_checks = 0
        self.average_response_time = 0
        self.portal_type_stats = {}
    
    def record_request(self, portal_type, success, response_time):
        self.requests_count += 1
        
        if success:
            self.successful_checks += 1
        else:
            self.failed_checks += 1
        
        # تحديث متوسط زمن الاستجابة
        self.average_response_time = (
            (self.average_response_time * (self.requests_count - 1) + response_time) 
            / self.requests_count
        )
        
        # إحصائيات نوع البوابة
        if portal_type not in self.portal_type_stats:
            self.portal_type_stats[portal_type] = {'success': 0, 'failed': 0}
        
        if success:
            self.portal_type_stats[portal_type]['success'] += 1
        else:
            self.portal_type_stats[portal_type]['failed'] += 1
    
    def get_success_rate(self):
        if self.requests_count == 0:
            return 0
        return (self.successful_checks / self.requests_count) * 100
```

---

## 🛡️ الأمان والحماية

### 🔐 Security Best Practices:

1. **Input Validation** - التحقق من صحة المدخلات
2. **Rate Limiting** - تحديد معدل الطلبات
3. **Authentication** - المصادقة للوصول للـ API
4. **Logging** - تسجيل العمليات
5. **Error Handling** - معالجة الأخطاء بأمان

### 🛡️ مثال Rate Limiting:

```python
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

limiter = Limiter(
    app,
    key_func=get_remote_address,
    default_limits=["200 per day", "50 per hour"]
)

@app.route('/api/discover', methods=['POST'])
@limiter.limit("10 per minute")
def discover_portal():
    # API logic here
    pass
```

---

## 🚀 النشر والتشغيل في الإنتاج

### 🐳 Docker Deployment:

```dockerfile
FROM python:3.9-slim

WORKDIR /app

COPY requirements_api.txt .
RUN pip install -r requirements_api.txt

COPY . .

EXPOSE 5000

CMD ["gunicorn", "--bind", "0.0.0.0:5000", "flask_iptv_api_server:app"]
```

### ⚙️ Production Configuration:

```python
# config.py
import os

class ProductionConfig:
    DEBUG = False
    TESTING = False
    
    # Database
    DATABASE_URL = os.environ.get('DATABASE_URL')
    
    # Security
    SECRET_KEY = os.environ.get('SECRET_KEY')
    
    # Rate Limiting
    RATELIMIT_STORAGE_URL = os.environ.get('REDIS_URL')
    
    # Logging
    LOG_LEVEL = 'INFO'
    LOG_FILE = '/var/log/iptv-api.log'
```

---

## 📋 خاتمة

### ✅ ما تم إنجازه:

1. **استخراج شامل** من 4 ملفات IPTV متخصصة
2. **API موحد** يدعم جميع البروتوكولات الشائعة
3. **Flask REST API** جاهز للإنتاج
4. **Android Integration** كامل مع أمثلة عملية
5. **45+ Portal Types** مدعومة
6. **Advanced Features** مثل Batch Processing و Geographic Detection

### 🎯 الفوائد الرئيسية:

- **سهولة التكامل** مع تطبيق Android الموجود
- **دعم شامل** لجميع بروتوكولات IPTV
- **أداء عالي** مع المعالجة المتوازية
- **مرونة كاملة** في التخصيص والتطوير
- **موثوقية عالية** مع معالجة الأخطاء المتقدمة

### 🚀 الخطوات التالية:

1. **تشغيل API Server** في بيئة الإنتاج
2. **تكامل Android App** مع الـ API الجديد
3. **إضافة المزيد من Features** حسب الحاجة
4. **مراقبة الأداء** وتحسين السرعة
5. **توسيع الدعم** لبروتوكولات إضافية

---

## 📞 الدعم والمساعدة

هذا API جاهز للاستخدام الفوري ويوفر جميع الوظائف المطلوبة لتطبيق IPTV متكامل. يمكن توسيعه وتخصيصه بسهولة حسب احتياجات المشروع.

**🎉 تطبيق IPTV الخاص بك الآن يدعم جميع بروتوكولات IPTV الشائعة مع API متقدم ومرن!**