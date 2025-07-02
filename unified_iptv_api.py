#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Unified IPTV API - واجهة برمجة تطبيقات IPTV موحدة
مستخرجة من تحليل عدة ملفات IPTV scripts

يدعم:
- Stalker Portal API
- Xtream Codes API  
- MAG Portal API
- M3U Parsing
- MAC Address Generation
- Portal Discovery
- Geographic Location Detection
"""

import requests
import hashlib
import random
import re
import json
import time
import datetime
from typing import Dict, List, Optional, Tuple
from urllib.parse import quote, urlparse
from urllib3.exceptions import InsecureRequestWarning

# تعطيل تحذيرات SSL
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

class UnifiedIPTVAPI:
    """
    واجهة IPTV موحدة تدعم جميع البروتوكولات الشائعة
    """
    
    # قائمة أنواع البوابات المدعومة
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
    
    # MAC Address Prefixes الشائعة لـ MAG devices
    MAC_PREFIXES = [
        '00:1A:79:', '78:A3:52:', '10:27:BE:', '6C:0D:C4:', 
        'A0:BB:3E:', 'D0:9F:D9:', '04:D6:AA:', '11:33:01:', 
        '00:1C:19:', '1A:00:6A:', '1A:00:FB:', '00:A1:79:',
        '00:1B:79:', '00:2A:79:', 'D4:CF:F9:', '33:44:CF:'
    ]
    
    def __init__(self, timeout: int = 15):
        self.timeout = timeout
        self.session = requests.Session()
        self.session.verify = False
        
    def discover_portal(self, host: str) -> Dict:
        """
        اكتشاف نوع البوابة وأفضل endpoint
        
        Args:
            host: عنوان الخادم
            
        Returns:
            Dict: معلومات البوابة المكتشفة
        """
        if '://' in host:
            host = host.split('://')[1]
        host = host.split('/')[0]
        
        user_agents = [
            'Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Safari/533.3',
            'Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 2 rev: 250 Safari/533.3',
            'Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 2721 Mobile Safari/533.3'
        ]
        
        best_result = {
            "success": False,
            "status_code": 0, 
            "endpoint": "",
            "portal_type": "",
            "host": host,
            "full_url": ""
        }
        
        for endpoint in self.PORTAL_TYPES:
            try:
                url = f"http://{host}{endpoint}"
                headers = {'User-Agent': random.choice(user_agents)}
                
                response = self.session.get(url, headers=headers, timeout=5)
                
                if response.status_code in [200, 401, 512]:
                    best_result.update({
                        "success": True,
                        "status_code": response.status_code,
                        "endpoint": endpoint,
                        "portal_type": self._detect_portal_type(endpoint),
                        "full_url": url
                    })
                    
                    # إذا وجدنا 200 أو 401، توقف (هذه أفضل النتائج)
                    if response.status_code in [200, 401]:
                        break
                        
            except Exception:
                continue
                
        return best_result
    
    def _detect_portal_type(self, endpoint: str) -> str:
        """تحديد نوع البوابة من المسار"""
        if "stalker_portal" in endpoint:
            return "stalker"
        elif "portal.php" in endpoint:
            return "mag_portal"
        elif "load.php" in endpoint:
            return "stalker_load"
        elif "ministra" in endpoint:
            return "ministra"
        elif "xtream" in endpoint:
            return "xtream"
        else:
            return "unknown"
    
    def generate_mac_address(self, prefix: str = None) -> str:
        """
        توليد عنوان MAC عشوائي
        
        Args:
            prefix: البادئة المطلوبة (اختياري)
            
        Returns:
            str: عنوان MAC
        """
        if not prefix:
            prefix = random.choice(self.MAC_PREFIXES)
            
        mac = prefix + "%02x:%02x:%02x" % (
            random.randint(0, 255),
            random.randint(0, 255), 
            random.randint(0, 255)
        )
        return mac.upper()
    
    def validate_mac_address(self, mac: str) -> bool:
        """التحقق من صحة عنوان MAC"""
        pattern = r'^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$'
        return bool(re.match(pattern, mac))
    
    def generate_device_credentials(self, mac: str) -> Dict:
        """
        توليد بيانات اعتماد الجهاز من عنوان MAC
        
        Args:
            mac: عنوان MAC
            
        Returns:
            Dict: بيانات الجهاز
        """
        mac_clean = mac.upper().replace(':', '')
        
        # Serial Number
        sn_hash = hashlib.md5(mac.encode()).hexdigest()
        serial_number = sn_hash.upper()[:13]
        
        # Device ID
        device_id = hashlib.sha256(mac.encode()).hexdigest().upper()
        
        # Device ID 2 (alternative)
        device_id2 = hashlib.sha256(serial_number.encode()).hexdigest().upper()
        
        # Signature
        signature_input = serial_number + mac
        signature = hashlib.sha256(signature_input.encode()).hexdigest().upper()
        
        return {
            'mac': mac.upper(),
            'mac_encoded': quote(mac.upper()),
            'serial_number': serial_number,
            'device_id': device_id,
            'device_id2': device_id2, 
            'signature': signature,
            'stb_type': 'MAG254'
        }

class StalkerPortalAPI:
    """
    Stalker Portal API wrapper
    """
    
    def __init__(self, host: str, mac: str, portal_path: str = "/stalker_portal/server/load.php"):
        self.host = host.replace('http://', '').replace('https://', '')
        self.mac = mac.upper()
        self.portal_path = portal_path
        self.token = None
        self.session = requests.Session()
        self.session.verify = False
        
        # توليد بيانات الجهاز
        api = UnifiedIPTVAPI()
        self.device_creds = api.generate_device_credentials(mac)
    
    def handshake(self) -> bool:
        """إجراء المصادقة الأولية"""
        url = f"http://{self.host}{self.portal_path}"
        params = {
            'type': 'stb',
            'action': 'handshake',
            'token': '',
            'prehash': 'false',
            'JsHttpRequest': '1-xml'
        }
        
        try:
            response = self.session.get(url, params=params, headers=self._get_headers())
            
            if 'token' in response.text:
                data = response.json()
                self.token = data['js']['token']
                return True
        except Exception:
            pass
            
        return False
    
    def get_profile(self) -> Optional[Dict]:
        """الحصول على معلومات الحساب"""
        if not self.token:
            return None
            
        url = f"http://{self.host}{self.portal_path}"
        params = {
            'type': 'stb',
            'action': 'get_profile',
            'JsHttpRequest': '1-xml'
        }
        
        try:
            response = self.session.get(url, params=params, headers=self._get_headers_with_token())
            return response.json()
        except Exception:
            return None
    
    def get_account_info(self) -> Optional[Dict]:
        """الحصول على معلومات الحساب التفصيلية"""
        if not self.token:
            return None
            
        url = f"http://{self.host}{self.portal_path}"
        params = {
            'type': 'account_info',
            'action': 'get_main_info',
            'JsHttpRequest': '1-xml'
        }
        
        try:
            response = self.session.get(url, params=params, headers=self._get_headers_with_token())
            return response.json()
        except Exception:
            return None
    
    def get_channels(self) -> Optional[List]:
        """الحصول على قائمة القنوات"""
        if not self.token:
            return None
            
        url = f"http://{self.host}{self.portal_path}"
        params = {
            'type': 'itv',
            'action': 'get_all_channels',
            'force_ch_link_check': '',
            'JsHttpRequest': '1-xml'
        }
        
        try:
            response = self.session.get(url, params=params, headers=self._get_headers_with_token())
            data = response.json()
            return data.get('js', {}).get('data', [])
        except Exception:
            return None
    
    def get_vod_categories(self) -> Optional[List]:
        """الحصول على فئات الأفلام"""
        if not self.token:
            return None
            
        url = f"http://{self.host}{self.portal_path}"
        params = {
            'action': 'get_categories',
            'type': 'vod',
            'JsHttpRequest': '1-xml'
        }
        
        try:
            response = self.session.get(url, params=params, headers=self._get_headers_with_token())
            data = response.json()
            return data.get('js', {}).get('data', [])
        except Exception:
            return None
    
    def get_series_categories(self) -> Optional[List]:
        """الحصول على فئات المسلسلات"""
        if not self.token:
            return None
            
        url = f"http://{self.host}{self.portal_path}"
        params = {
            'action': 'get_categories',
            'type': 'series',
            'JsHttpRequest': '1-xml'
        }
        
        try:
            response = self.session.get(url, params=params, headers=self._get_headers_with_token())
            data = response.json()
            return data.get('js', {}).get('data', [])
        except Exception:
            return None
    
    def create_channel_link(self, channel_id: str) -> Optional[str]:
        """إنشاء رابط تشغيل للقناة"""
        if not self.token:
            return None
            
        url = f"http://{self.host}{self.portal_path}"
        params = {
            'type': 'itv',
            'action': 'create_link',
            'cmd': f'ffmpeg http://localhost/ch/{channel_id}_',
            'JsHttpRequest': '1-xml'
        }
        
        try:
            response = self.session.get(url, params=params, headers=self._get_headers_with_token())
            data = response.json()
            
            if 'cmd' in data.get('js', {}):
                return data['js']['cmd']
        except Exception:
            pass
            
        return None
    
    def _get_headers(self) -> Dict:
        """Headers أساسية للطلبات"""
        return {
            'User-Agent': 'Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Mobile Safari/533.3',
            'Referer': f'http://{self.host}/c/',
            'Accept': 'application/json,application/javascript,text/javascript,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Cookie': f'mac={self.device_creds["mac_encoded"]}; stb_lang=en; timezone=Europe/Paris;',
            'Accept-Encoding': 'gzip, deflate',
            'Connection': 'Keep-Alive',
            'X-User-Agent': 'Model: MAG254; Link: Ethernet'
        }
    
    def _get_headers_with_token(self) -> Dict:
        """Headers مع التوكن للطلبات المحمية"""
        headers = self._get_headers()
        if self.token:
            headers['Authorization'] = f'Bearer {self.token}'
        return headers

class XtreamCodesAPI:
    """
    Xtream Codes API wrapper
    """
    
    def __init__(self, host: str, username: str, password: str):
        self.host = host.replace('http://', '').replace('https://', '')
        self.username = username
        self.password = password
        self.session = requests.Session()
        self.session.verify = False
    
    def authenticate(self) -> Dict:
        """التحقق من صحة بيانات الاعتماد"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password
        }
        
        try:
            response = self.session.get(url, params=params, timeout=10)
            data = response.json()
            
            return {
                'success': 'user_info' in data,
                'data': data if 'user_info' in data else None,
                'user_info': data.get('user_info', {}),
                'server_info': data.get('server_info', {})
            }
        except Exception as e:
            return {
                'success': False,
                'error': str(e),
                'data': None
            }
    
    def get_live_streams(self) -> List:
        """الحصول على القنوات المباشرة"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_live_streams'
        }
        
        try:
            response = self.session.get(url, params=params)
            return response.json()
        except Exception:
            return []
    
    def get_vod_streams(self) -> List:
        """الحصول على الأفلام"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_vod_streams'
        }
        
        try:
            response = self.session.get(url, params=params)
            return response.json()
        except Exception:
            return []
    
    def get_series(self) -> List:
        """الحصول على المسلسلات"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_series'
        }
        
        try:
            response = self.session.get(url, params=params)
            return response.json()
        except Exception:
            return []
    
    def get_live_categories(self) -> List:
        """الحصول على فئات القنوات"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_live_categories'
        }
        
        try:
            response = self.session.get(url, params=params)
            return response.json()
        except Exception:
            return []
    
    def get_vod_categories(self) -> List:
        """الحصول على فئات الأفلام"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_vod_categories'
        }
        
        try:
            response = self.session.get(url, params=params)
            return response.json()
        except Exception:
            return []
    
    def get_series_categories(self) -> List:
        """الحصول على فئات المسلسلات"""
        url = f"http://{self.host}/player_api.php"
        params = {
            'username': self.username,
            'password': self.password,
            'action': 'get_series_categories'
        }
        
        try:
            response = self.session.get(url, params=params)
            return response.json()
        except Exception:
            return []
    
    def generate_m3u_url(self) -> str:
        """توليد رابط M3U"""
        return f"http://{self.host}/get.php?username={self.username}&password={self.password}&type=m3u_plus"
    
    def generate_xmltv_url(self) -> str:
        """توليد رابط XMLTV للـ EPG"""
        return f"http://{self.host}/xmltv.php?username={self.username}&password={self.password}"

class M3UParser:
    """
    محلل ملفات M3U
    """
    
    def __init__(self, content: str = None, url: str = None):
        self.content = content
        self.url = url
        self.channels = []
    
    def parse(self) -> List[Dict]:
        """تحليل محتوى M3U"""
        if not self.content and self.url:
            self.content = self._fetch_content()
        
        if not self.content:
            return []
        
        lines = self.content.split('\n')
        current_channel = {}
        
        for line in lines:
            line = line.strip()
            
            if line.startswith('#EXTINF:'):
                # تحليل معلومات القناة
                parts = line.split(',', 1)
                if len(parts) == 2:
                    current_channel['name'] = parts[1].strip()
                    # استخراج المعاملات الإضافية
                    self._extract_extinf_params(parts[0], current_channel)
            
            elif line.startswith('http'):
                # رابط القناة
                current_channel['url'] = line
                self.channels.append(current_channel.copy())
                current_channel = {}
        
        return self.channels
    
    def _fetch_content(self) -> str:
        """جلب المحتوى من URL"""
        try:
            response = requests.get(self.url, timeout=30)
            return response.text
        except Exception:
            return ""
    
    def _extract_extinf_params(self, extinf_line: str, channel: Dict):
        """استخراج المعاملات من سطر EXTINF"""
        patterns = {
            'tvg-id': r'tvg-id="([^"]*)"',
            'tvg-name': r'tvg-name="([^"]*)"',
            'tvg-logo': r'tvg-logo="([^"]*)"',
            'group-title': r'group-title="([^"]*)"',
            'tvg-chno': r'tvg-chno="([^"]*)"',
            'tvg-shift': r'tvg-shift="([^"]*)"'
        }
        
        for param, pattern in patterns.items():
            match = re.search(pattern, extinf_line, re.IGNORECASE)
            if match:
                channel[param] = match.group(1)

class GeoLocationAPI:
    """
    واجهة برمجة تطبيقات الموقع الجغرافي
    """
    
    @staticmethod
    def get_server_info(ip_or_host: str) -> Optional[Dict]:
        """الحصول على معلومات الموقع الجغرافي"""
        # استخراج IP من hostname إذا لزم الأمر
        if not re.match(r'^\d+\.\d+\.\d+\.\d+$', ip_or_host):
            try:
                import socket
                ip_or_host = socket.gethostbyname(ip_or_host)
            except:
                pass
        
        try:
            url = f"https://ipleak.net/json/{ip_or_host}"
            response = requests.get(url, timeout=10)
            data = response.json()
            
            return {
                'ip': data.get('ip', ''),
                'country': data.get('country_name', ''),
                'country_code': data.get('country_code', ''),
                'city': data.get('city_name', ''),
                'region': data.get('region_name', ''),
                'isp': data.get('isp_name', ''),
                'continent': data.get('continent_name', ''),
                'timezone': data.get('time_zone', ''),
                'latitude': data.get('latitude', ''),
                'longitude': data.get('longitude', '')
            }
        except Exception:
            return None
    
    @staticmethod
    def get_country_flag(country_code: str) -> str:
        """الحصول على علم الدولة"""
        try:
            # تحويل رمز الدولة إلى emoji العلم
            flag_offset = 0x1F1E6 - ord('A')
            flag = ''.join(chr(ord(char) + flag_offset) for char in country_code.upper())
            return flag
        except:
            return ""

class IPTVUtilities:
    """
    وظائف مساعدة عامة
    """
    
    @staticmethod
    def check_stream_availability(url: str, timeout: int = 5) -> bool:
        """فحص توفر الرابط"""
        try:
            headers = {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                'Range': 'bytes=0-1024'
            }
            response = requests.head(url, headers=headers, timeout=timeout, allow_redirects=True)
            return response.status_code in [200, 206, 302, 406]
        except Exception:
            return False
    
    @staticmethod
    def extract_credentials_from_url(url: str) -> Optional[Dict]:
        """استخراج بيانات الاعتماد من الرابط"""
        patterns = [
            r'live/(\w+)/(\w+)/',  # Xtream Codes live
            r'movie/(\w+)/(\w+)/',  # Xtream Codes movie
            r'/(\w+)/(\w+)/\d+',    # General pattern
        ]
        
        for pattern in patterns:
            match = re.search(pattern, url)
            if match:
                return {
                    'username': match.group(1),
                    'password': match.group(2)
                }
        return None
    
    @staticmethod
    def format_expiry_date(timestamp) -> str:
        """تنسيق تاريخ الانتهاء"""
        try:
            if not timestamp or timestamp == "null":
                return "Unlimited"
            
            if isinstance(timestamp, str):
                if timestamp.isdigit():
                    timestamp = int(timestamp)
                else:
                    # محاولة تحليل التاريخ النصي
                    return str(timestamp)
            
            if isinstance(timestamp, (int, float)):
                dt = datetime.datetime.fromtimestamp(timestamp)
                return dt.strftime('%d-%m-%Y %H:%M:%S')
            
            return str(timestamp)
        except Exception:
            return "Invalid Date"
    
    @staticmethod
    def calculate_days_remaining(expiry_date: str) -> int:
        """حساب الأيام المتبقية"""
        try:
            if "unlimited" in expiry_date.lower():
                return -1  # غير محدود
            
            # محاولة تحليل التاريخ
            formats = ['%d-%m-%Y %H:%M:%S', '%Y-%m-%d %H:%M:%S', '%d/%m/%Y']
            
            for fmt in formats:
                try:
                    exp_dt = datetime.datetime.strptime(expiry_date, fmt)
                    now = datetime.datetime.now()
                    delta = exp_dt - now
                    return delta.days
                except ValueError:
                    continue
            
            return 0
        except Exception:
            return 0
    
    @staticmethod
    def clean_channel_name(name: str) -> str:
        """تنظيف اسم القناة"""
        # إزالة الرموز غير المرغوب فيها
        name = re.sub(r'[^\w\s\-\[\]()]', '', name)
        # إزالة المسافات الزائدة
        name = ' '.join(name.split())
        return name.strip()
    
    @staticmethod
    def categorize_by_name(channel_name: str) -> str:
        """تصنيف القناة حسب الاسم"""
        name_lower = channel_name.lower()
        
        if any(word in name_lower for word in ['sport', 'espn', 'bein', 'fox sport', 'sky sport']):
            return 'Sports'
        elif any(word in name_lower for word in ['news', 'cnn', 'bbc', 'fox news', 'al jazeera']):
            return 'News'
        elif any(word in name_lower for word in ['movie', 'cinema', 'film', 'hollywood']):
            return 'Movies'
        elif any(word in name_lower for word in ['kids', 'cartoon', 'disney', 'nickelodeon']):
            return 'Kids'
        elif any(word in name_lower for word in ['music', 'mtv', 'vh1']):
            return 'Music'
        elif any(word in name_lower for word in ['hd', '4k', 'uhd']):
            return 'HD'
        else:
            return 'General'

# مثال للاستخدام
if __name__ == "__main__":
    # إنشاء مثيل من API الموحد
    api = UnifiedIPTVAPI()
    
    # اكتشاف نوع البوابة
    portal_info = api.discover_portal("example.com:8080")
    print("Portal Discovery:", portal_info)
    
    # توليد عنوان MAC
    mac = api.generate_mac_address()
    print("Generated MAC:", mac)
    
    # استخدام Stalker Portal API
    if portal_info['success'] and portal_info['portal_type'] == 'stalker':
        stalker = StalkerPortalAPI(
            host=portal_info['host'],
            mac=mac,
            portal_path=portal_info['endpoint']
        )
        
        if stalker.handshake():
            print("Stalker handshake successful")
            
            profile = stalker.get_profile()
            if profile:
                print("Profile:", profile)
    
    # استخدام Xtream Codes API
    xtream = XtreamCodesAPI("example.com", "username", "password")
    auth_result = xtream.authenticate()
    print("Xtream Auth:", auth_result)
    
    # تحليل M3U
    m3u_parser = M3UParser(url="http://example.com/playlist.m3u")
    channels = m3u_parser.parse()
    print(f"Found {len(channels)} channels in M3U")