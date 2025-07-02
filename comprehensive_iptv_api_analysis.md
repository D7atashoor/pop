# تحليل شامل لملفات IPTV - Comprehensive IPTV Scripts Analysis

## 📋 نظرة عامة

تم تحليل 4 ملفات متخصصة في فحص واختبار خدمات IPTV:

### 📁 الملفات المحللة:
1. **ViagraUltra_Portal_Check_2024.txt** - فاحص بوابات متقدم مع دعم MAC scanning
2. **A_pxll313_√ᵖʳᵒ³_Flag-dec.txt** - فاحص شامل لبروتوكولات متعددة 
3. **A_pxlSTB9GOLD2Premium.txt** - (سيتم تحليله بالتفصيل)
4. **multi_list_micmacv5.txt** - (سيتم تحليله بالتفصيل)

---

## 🔍 التحليل التفصيلي

### 1️⃣ ViagraUltra Portal Checker

#### الميزات الرئيسية:
- **فحص البوابات التلقائي**: يدعم 45+ نوع بوابة مختلفة
- **MAC Address Generation**: توليد عناوين MAC عشوائية أو متسلسلة
- **Multi-threading**: دعم حتى 15 bot متزامن
- **Portal Discovery**: اكتشاف نوع البوابة تلقائياً

#### APIs المستخرجة:

```python
# Portal Types المدعومة
PORTAL_TYPES = [
    '/portal.php',
    '/server/load.php', 
    '/stalker_portal/server/load.php',
    '/stalker_u.php',
    '/BoSSxxxx/portal.php',
    '/c/portal.php',
    '/c/server/load.php',
    '/magaccess/portal.php',
    '/portalcc.php',
    '/bs.mag.portal.php',
    '/magportal/portal.php',
    '/maglove/portal.php',
    '/tek/server/load.php',
    '/emu/server/load.php',
    '/emu2/server/load.php',
    '/xx//server/load.php',
    '/portalott.php',
    '/ghandi_portal/server/load.php',
    '/magLoad.php',
    '/ministra/portal.php',
    '/portalstb/portal.php',
    '/xx/portal.php',
    '/portalmega.php',
    '/portalmega/portal.php',
    '/rmxportal/portal.php',
    '/portalmega/portalmega.php',
    '/powerfull/portal.php',
    '/korisnici/server/load.php',
    '/nettvmag/portal.php',
    '/cmdforex/portal.php',
    '/k/portal.php',
    '/p/portal.php',
    '/cp/server/load.php',
    '/extraportal.php',
    '/Link_Ok/portal.php',
    '/delko/portal.php',
    '/delko/server/load.php',
    '/bStream/portal.php',
    '/bStream/server/load.php',
    '/blowportal/portal.php',
    '/client/portal.php',
    '/server/move.php'
]

# MAC Address Generation
def generate_mac_address(prefix="00:1A:79:"):
    """توليد عنوان MAC عشوائي"""
    import random
    mac = prefix + "%02x:%02x:%02x" % (
        random.randint(0, 255),
        random.randint(0, 255), 
        random.randint(0, 255)
    )
    return mac.upper()

# Portal Discovery Function
def discover_portal_type(host):
    """اكتشاف نوع البوابة"""
    import requests
    
    user_agents = [
        'Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Safari/533.3',
        'Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 2 rev: 250 Safari/533.3',
        'Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 2721 Mobile Safari/533.3'
    ]
    
    best_result = {"status_code": 0, "endpoint": "", "portal_type": ""}
    
    for endpoint in PORTAL_TYPES:
        try:
            url = f"http://{host}{endpoint}"
            headers = {'User-Agent': random.choice(user_agents)}
            response = requests.get(url, headers=headers, timeout=5)
            
            if response.status_code in [200, 401]:
                best_result = {
                    "status_code": response.status_code,
                    "endpoint": endpoint,
                    "portal_type": detect_portal_type(endpoint)
                }
                break
                
        except Exception:
            continue
            
    return best_result

def detect_portal_type(endpoint):
    """تحديد نوع البوابة من المسار"""
    if "stalker_portal" in endpoint:
        return "stalker"
    elif "portal.php" in endpoint:
        return "mag_portal"
    elif "load.php" in endpoint:
        return "stalker_load"
    elif "ministra" in endpoint:
        return "ministra"
    else:
        return "unknown"
```

### 2️⃣ A_pxl Portal Checker Advanced

#### الميزات المتقدمة:
- **Multi-Protocol Support**: Stalker Portal, MAG Portal, Ministra
- **Advanced Authentication**: Token-based authentication
- **Channel Parsing**: استخراج قوائم القنوات والأفلام
- **Geographic Detection**: كشف موقع الخادم والعميل
- **M3U Generation**: توليد روابط M3U تلقائياً

#### APIs المستخرجة:

```python
class StalkerPortalAPI:
    def __init__(self, host, mac_address):
        self.host = host
        self.mac = mac_address.upper()
        self.token = None
        
    def handshake(self):
        """إجراء المصادقة الأولية"""
        url = f"http://{self.host}/stalker_portal/server/load.php"
        params = {
            'type': 'stb',
            'action': 'handshake',
            'token': '',
            'prehash': 'false',
            'JsHttpRequest': '1-xml'
        }
        
        headers = self._get_headers()
        response = requests.get(url, params=params, headers=headers)
        
        if 'token' in response.text:
            self.token = response.json()['js']['token']
            return True
        return False
        
    def get_profile(self):
        """الحصول على معلومات الحساب"""
        if not self.token:
            return None
            
        url = f"http://{self.host}/stalker_portal/server/load.php"
        params = {
            'type': 'stb',
            'action': 'get_profile',
            'JsHttpRequest': '1-xml'
        }
        
        headers = self._get_headers_with_token()
        response = requests.get(url, params=params, headers=headers)
        return response.json()
        
    def get_channels(self):
        """الحصول على قائمة القنوات"""
        url = f"http://{self.host}/stalker_portal/server/load.php"
        params = {
            'type': 'itv',
            'action': 'get_all_channels',
            'force_ch_link_check': '',
            'JsHttpRequest': '1-xml'
        }
        
        headers = self._get_headers_with_token()
        response = requests.get(url, params=params, headers=headers)
        return response.json()
        
    def get_vod_categories(self):
        """الحصول على فئات الأفلام"""
        url = f"http://{self.host}/stalker_portal/server/load.php"
        params = {
            'action': 'get_categories',
            'type': 'vod',
            'JsHttpRequest': '1-xml'
        }
        
        headers = self._get_headers_with_token()
        response = requests.get(url, params=params, headers=headers)
        return response.json()
        
    def get_series_categories(self):
        """الحصول على فئات المسلسلات"""
        url = f"http://{self.host}/stalker_portal/server/load.php"
        params = {
            'action': 'get_categories', 
            'type': 'series',
            'JsHttpRequest': '1-xml'
        }
        
        headers = self._get_headers_with_token()
        response = requests.get(url, params=params, headers=headers)
        return response.json()
        
    def create_channel_link(self, channel_id):
        """إنشاء رابط تشغيل للقناة"""
        url = f"http://{self.host}/stalker_portal/server/load.php"
        params = {
            'type': 'itv',
            'action': 'create_link',
            'cmd': f'ffmpeg http://localhost/ch/{channel_id}_',
            'JsHttpRequest': '1-xml'
        }
        
        headers = self._get_headers_with_token()
        response = requests.get(url, params=params, headers=headers)
        return response.json()
        
    def _get_headers(self):
        """Headers أساسية للطلبات"""
        return {
            'User-Agent': 'Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3',
            'Referer': f'http://{self.host}/c/',
            'Accept': 'application/json,application/javascript,text/javascript,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Cookie': f'mac={self.mac.replace(":", "%3A")}; stb_lang=en; timezone=Europe/Paris;',
            'Accept-Encoding': 'gzip, deflate',
            'Connection': 'Keep-Alive',
            'X-User-Agent': 'Model: MAG254; Link: Ethernet'
        }
        
    def _get_headers_with_token(self):
        """Headers مع التوكن للطلبات المحمية"""
        headers = self._get_headers()
        headers['Authorization'] = f'Bearer {self.token}'
        return headers

class XtreamCodeAPI:
    def __init__(self, host, username, password):
        self.host = host
        self.username = username
        self.password = password
        
    def authenticate(self):
        """التحقق من صحة بيانات الاعتماد"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password
        }
        
        try:
            response = requests.get(url, params=params, timeout=10)
            data = response.json()
            return 'user_info' in data
        except:
            return False
            
    def get_live_streams(self):
        """الحصول على القنوات المباشرة"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_live_streams'
        }
        
        response = requests.get(url, params=params)
        return response.json()
        
    def get_vod_streams(self):
        """الحصول على الأفلام"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_vod_streams'
        }
        
        response = requests.get(url, params=params)
        return response.json()
        
    def get_series(self):
        """الحصول على المسلسلات"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_series'
        }
        
        response = requests.get(url, params=params)
        return response.json()
        
    def get_live_categories(self):
        """الحصول على فئات القنوات"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_live_categories'
        }
        
        response = requests.get(url, params=params)
        return response.json()
        
    def generate_m3u(self):
        """توليد رابط M3U"""
        return f"http://{self.host}/get.php?username={self.username}&password={self.password}&type=m3u_plus"
        
    def generate_xmltv(self):
        """توليد رابط XMLTV للـ EPG"""
        return f"http://{self.host}/xmltv.php?username={self.username}&password={self.password}"

class M3UParser:
    def __init__(self, content):
        self.content = content
        self.channels = []
        
    def parse(self):
        """تحليل محتوى M3U"""
        lines = self.content.split('\n')
        current_channel = {}
        
        for line in lines:
            line = line.strip()
            
            if line.startswith('#EXTINF:'):
                # تحليل معلومات القناة
                parts = line.split(',', 1)
                if len(parts) == 2:
                    current_channel['name'] = parts[1]
                    # استخراج المعاملات الإضافية
                    self._extract_extinf_params(parts[0], current_channel)
                    
            elif line.startswith('http'):
                # رابط القناة
                current_channel['url'] = line
                self.channels.append(current_channel.copy())
                current_channel = {}
                
        return self.channels
        
    def _extract_extinf_params(self, extinf_line, channel):
        """استخراج المعاملات من سطر EXTINF"""
        import re
        
        # استخراج المعاملات المختلفة
        patterns = {
            'tvg-id': r'tvg-id="([^"]*)"',
            'tvg-name': r'tvg-name="([^"]*)"', 
            'tvg-logo': r'tvg-logo="([^"]*)"',
            'group-title': r'group-title="([^"]*)"',
            'tvg-chno': r'tvg-chno="([^"]*)"'
        }
        
        for param, pattern in patterns.items():
            match = re.search(pattern, extinf_line)
            if match:
                channel[param] = match.group(1)

class GeoLocationAPI:
    @staticmethod
    def get_server_info(ip_address):
        """الحصول على معلومات الموقع الجغرافي للخادم"""
        try:
            url = f"https://ipleak.net/json/{ip_address}"
            response = requests.get(url, timeout=10)
            data = response.json()
            
            return {
                'ip': data.get('ip', ''),
                'country': data.get('country_name', ''),
                'country_code': data.get('country_code', ''),
                'city': data.get('city_name', ''),
                'region': data.get('region_name', ''),
                'isp': data.get('isp_name', ''),
                'continent': data.get('continent_name', '')
            }
        except:
            return None
            
    @staticmethod
    def get_country_flag(country_code):
        """الحصول على علم الدولة"""
        try:
            import flag
            return flag.flag(country_code)
        except:
            return ""
```

---

## 🔧 الوظائف المساعدة

```python
class IPTVUtilities:
    @staticmethod
    def validate_mac_address(mac):
        """التحقق من صحة عنوان MAC"""
        import re
        pattern = r'^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$'
        return bool(re.match(pattern, mac))
        
    @staticmethod
    def generate_device_id(mac):
        """توليد معرف الجهاز من عنوان MAC"""
        import hashlib
        return hashlib.sha256(mac.encode()).hexdigest().upper()
        
    @staticmethod
    def generate_serial_number(mac):
        """توليد الرقم التسلسلي من عنوان MAC"""
        import hashlib
        md5_hash = hashlib.md5(mac.encode()).hexdigest()
        return md5_hash.upper()[:13]
        
    @staticmethod
    def check_stream_availability(url):
        """فحص توفر الرابط"""
        try:
            response = requests.head(url, timeout=5)
            return response.status_code in [200, 302, 406]
        except:
            return False
            
    @staticmethod
    def extract_credentials_from_url(url):
        """استخراج بيانات الاعتماد من الرابط"""
        import re
        
        # استخراج username و password من روابط Xtream
        pattern = r'live/(\w+)/(\w+)/'
        match = re.search(pattern, url)
        
        if match:
            return {
                'username': match.group(1),
                'password': match.group(2)
            }
        return None
        
    @staticmethod
    def format_expiry_date(timestamp):
        """تنسيق تاريخ الانتهاء"""
        import datetime
        
        try:
            if timestamp == "null" or not timestamp:
                return "Unlimited"
            
            if isinstance(timestamp, str) and timestamp.isdigit():
                timestamp = int(timestamp)
                
            if isinstance(timestamp, int):
                dt = datetime.datetime.fromtimestamp(timestamp)
                return dt.strftime('%d-%m-%Y %H:%M:%S')
            
            return str(timestamp)
        except:
            return "Invalid Date"
```

---

## 🎯 الخطوات التالية

### لتكامل كامل مع التطبيق:

1. **تحليل الملفين المتبقيين** لاستخراج المزيد من APIs
2. **إنشاء Flask API موحد** يجمع كل الوظائف
3. **إنشاء Android SDK** للتكامل المباشر
4. **إضافة دعم لبروتوكولات إضافية**

### المتطلبات الإضافية:
- `requests` - للطلبات HTTP
- `hashlib` - لتوليد المعرفات
- `datetime` - لمعالجة التواريخ
- `re` - للتعبيرات النمطية
- `flag` - لأعلام الدول (اختياري)

هذا التحليل يوفر أساساً قوياً لبناء نظام IPTV متكامل يدعم جميع البروتوكولات الشائعة.