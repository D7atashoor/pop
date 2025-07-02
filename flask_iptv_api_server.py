#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Flask IPTV API Server - خادم واجهة برمجة تطبيقات IPTV
REST API wrapper for the Unified IPTV API

Features:
- Portal Discovery
- Stalker Portal Integration
- Xtream Codes Integration
- M3U Parsing
- MAC Address Generation
- Geographic Location Detection
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import threading
import time
import logging
from unified_iptv_api import (
    UnifiedIPTVAPI,
    StalkerPortalAPI, 
    XtreamCodesAPI,
    M3UParser,
    GeoLocationAPI,
    IPTVUtilities
)

# تكوين Flask
app = Flask(__name__)
CORS(app)  # تمكين CORS لجميع الطلبات

# تكوين السجلات
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# إنشاء مثيل من API الموحد
iptv_api = UnifiedIPTVAPI()

# متغيرات لتتبع المهام
active_tasks = {}
task_counter = 0

class TaskManager:
    """إدارة المهام المتزامنة"""
    
    def __init__(self):
        self.tasks = {}
        self.counter = 0
    
    def create_task(self, task_type: str, total_items: int = 0) -> str:
        """إنشاء مهمة جديدة"""
        self.counter += 1
        task_id = f"task_{self.counter}"
        
        self.tasks[task_id] = {
            'id': task_id,
            'type': task_type,
            'status': 'pending',
            'progress': 0,
            'total': total_items,
            'results': [],
            'error': None,
            'created_at': time.time()
        }
        
        return task_id
    
    def update_task(self, task_id: str, **kwargs):
        """تحديث حالة المهمة"""
        if task_id in self.tasks:
            self.tasks[task_id].update(kwargs)
    
    def get_task(self, task_id: str) -> dict:
        """الحصول على معلومات المهمة"""
        return self.tasks.get(task_id, {})
    
    def cleanup_old_tasks(self, max_age: int = 3600):
        """تنظيف المهام القديمة"""
        current_time = time.time()
        to_remove = []
        
        for task_id, task in self.tasks.items():
            if current_time - task['created_at'] > max_age:
                to_remove.append(task_id)
        
        for task_id in to_remove:
            del self.tasks[task_id]

# إنشاء مدير المهام
task_manager = TaskManager()

# ============================================================================
# API Endpoints
# ============================================================================

@app.route('/')
def index():
    """الصفحة الرئيسية"""
    return jsonify({
        'service': 'IPTV Unified API Server',
        'version': '1.0.0',
        'status': 'running',
        'endpoints': {
            'portal_discovery': '/api/discover',
            'stalker_check': '/api/stalker/check',
            'xtream_check': '/api/xtream/check',
            'm3u_parse': '/api/m3u/parse',
            'generate_mac': '/api/utils/generate_mac',
            'geo_location': '/api/utils/geo_location',
            'task_status': '/api/task/<task_id>'
        }
    })

@app.route('/api/discover', methods=['POST'])
def discover_portal():
    """
    اكتشاف نوع البوابة
    """
    try:
        data = request.get_json()
        
        if not data or 'host' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing required field: host'
            }), 400
        
        host = data['host']
        result = iptv_api.discover_portal(host)
        
        # إضافة معلومات جغرافية إضافية
        if result['success']:
            geo_info = GeoLocationAPI.get_server_info(result['host'])
            if geo_info:
                result['geo_info'] = geo_info
        
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"Portal discovery error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/stalker/check', methods=['POST'])
def check_stalker():
    """
    فحص Stalker Portal
    """
    try:
        data = request.get_json()
        
        required_fields = ['host', 'mac']
        missing_fields = [field for field in required_fields if field not in data]
        
        if missing_fields:
            return jsonify({
                'success': False,
                'error': f'Missing required fields: {", ".join(missing_fields)}'
            }), 400
        
        host = data['host']
        mac = data['mac']
        portal_path = data.get('portal_path', '/stalker_portal/server/load.php')
        
        # التحقق من صحة MAC
        if not iptv_api.validate_mac_address(mac):
            return jsonify({
                'success': False,
                'error': 'Invalid MAC address format'
            }), 400
        
        # إنشاء API instance
        stalker = StalkerPortalAPI(host, mac, portal_path)
        
        # محاولة المصادقة
        if not stalker.handshake():
            return jsonify({
                'success': False,
                'error': 'Handshake failed - invalid MAC or server issue'
            })
        
        # الحصول على معلومات الحساب
        profile = stalker.get_profile()
        account_info = stalker.get_account_info()
        
        result = {
            'success': True,
            'host': host,
            'mac': mac,
            'portal_path': portal_path,
            'token': stalker.token,
            'profile': profile,
            'account_info': account_info
        }
        
        # الحصول على القنوات إذا كان مطلوباً
        if data.get('include_channels', False):
            channels = stalker.get_channels()
            result['channels'] = channels
            result['channels_count'] = len(channels) if channels else 0
        
        # الحصول على فئات VOD إذا كان مطلوباً
        if data.get('include_vod', False):
            vod_categories = stalker.get_vod_categories()
            result['vod_categories'] = vod_categories
        
        # الحصول على فئات المسلسلات إذا كان مطلوباً
        if data.get('include_series', False):
            series_categories = stalker.get_series_categories()
            result['series_categories'] = series_categories
        
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"Stalker check error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/xtream/check', methods=['POST'])
def check_xtream():
    """
    فحص Xtream Codes
    """
    try:
        data = request.get_json()
        
        required_fields = ['host', 'username', 'password']
        missing_fields = [field for field in required_fields if field not in data]
        
        if missing_fields:
            return jsonify({
                'success': False,
                'error': f'Missing required fields: {", ".join(missing_fields)}'
            }), 400
        
        host = data['host']
        username = data['username']
        password = data['password']
        
        # إنشاء API instance
        xtream = XtreamCodesAPI(host, username, password)
        
        # محاولة المصادقة
        auth_result = xtream.authenticate()
        
        if not auth_result['success']:
            return jsonify({
                'success': False,
                'error': 'Authentication failed - invalid credentials',
                'details': auth_result.get('error')
            })
        
        result = {
            'success': True,
            'host': host,
            'username': username,
            'user_info': auth_result['user_info'],
            'server_info': auth_result['server_info'],
            'm3u_url': xtream.generate_m3u_url(),
            'xmltv_url': xtream.generate_xmltv_url()
        }
        
        # الحصول على المحتوى إذا كان مطلوباً
        if data.get('include_content', False):
            live_streams = xtream.get_live_streams()
            vod_streams = xtream.get_vod_streams()
            series = xtream.get_series()
            
            result.update({
                'live_streams_count': len(live_streams),
                'vod_streams_count': len(vod_streams),
                'series_count': len(series),
                'live_categories': xtream.get_live_categories(),
                'vod_categories': xtream.get_vod_categories(),
                'series_categories': xtream.get_series_categories()
            })
            
            # إضافة المحتوى الفعلي إذا كان مطلوباً
            if data.get('include_full_content', False):
                result.update({
                    'live_streams': live_streams[:100],  # الحد الأقصى 100
                    'vod_streams': vod_streams[:100],
                    'series': series[:100]
                })
        
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"Xtream check error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/m3u/parse', methods=['POST'])
def parse_m3u():
    """
    تحليل ملف M3U
    """
    try:
        data = request.get_json()
        
        if not data or ('content' not in data and 'url' not in data):
            return jsonify({
                'success': False,
                'error': 'Missing required field: content or url'
            }), 400
        
        # إنشاء المحلل
        content = data.get('content')
        url = data.get('url')
        
        parser = M3UParser(content=content, url=url)
        channels = parser.parse()
        
        result = {
            'success': True,
            'channels_count': len(channels),
            'channels': channels
        }
        
        # إضافة إحصائيات
        if channels:
            categories = {}
            for channel in channels:
                category = channel.get('group-title', 'Unknown')
                categories[category] = categories.get(category, 0) + 1
            
            result['categories'] = categories
            result['categories_count'] = len(categories)
        
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"M3U parse error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/utils/generate_mac', methods=['GET', 'POST'])
def generate_mac():
    """
    توليد عنوان MAC عشوائي
    """
    try:
        data = request.get_json() if request.method == 'POST' else {}
        
        prefix = data.get('prefix') if data else request.args.get('prefix')
        count = int(data.get('count', 1) if data else request.args.get('count', 1))
        
        # الحد الأقصى للعدد
        count = min(count, 100)
        
        macs = []
        for _ in range(count):
            mac = iptv_api.generate_mac_address(prefix)
            device_creds = iptv_api.generate_device_credentials(mac)
            macs.append({
                'mac': mac,
                'device_credentials': device_creds
            })
        
        result = {
            'success': True,
            'count': len(macs),
            'macs': macs[0] if count == 1 else macs
        }
        
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"MAC generation error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/utils/geo_location', methods=['POST'])
def get_geo_location():
    """
    الحصول على معلومات الموقع الجغرافي
    """
    try:
        data = request.get_json()
        
        if not data or 'ip_or_host' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing required field: ip_or_host'
            }), 400
        
        ip_or_host = data['ip_or_host']
        geo_info = GeoLocationAPI.get_server_info(ip_or_host)
        
        if geo_info:
            # إضافة علم الدولة
            if geo_info['country_code']:
                geo_info['country_flag'] = GeoLocationAPI.get_country_flag(geo_info['country_code'])
            
            return jsonify({
                'success': True,
                'geo_info': geo_info
            })
        else:
            return jsonify({
                'success': False,
                'error': 'Unable to retrieve geographic information'
            })
        
    except Exception as e:
        logger.error(f"Geo location error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/utils/check_stream', methods=['POST'])
def check_stream():
    """
    فحص توفر رابط البث
    """
    try:
        data = request.get_json()
        
        if not data or 'url' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing required field: url'
            }), 400
        
        url = data['url']
        timeout = data.get('timeout', 5)
        
        is_available = IPTVUtilities.check_stream_availability(url, timeout)
        
        return jsonify({
            'success': True,
            'url': url,
            'available': is_available
        })
        
    except Exception as e:
        logger.error(f"Stream check error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/utils/extract_credentials', methods=['POST'])
def extract_credentials():
    """
    استخراج بيانات الاعتماد من الرابط
    """
    try:
        data = request.get_json()
        
        if not data or 'url' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing required field: url'
            }), 400
        
        url = data['url']
        credentials = IPTVUtilities.extract_credentials_from_url(url)
        
        if credentials:
            return jsonify({
                'success': True,
                'credentials': credentials
            })
        else:
            return jsonify({
                'success': False,
                'error': 'No credentials found in URL'
            })
        
    except Exception as e:
        logger.error(f"Credentials extraction error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/task/<task_id>')
def get_task_status(task_id):
    """
    الحصول على حالة المهمة
    """
    try:
        task = task_manager.get_task(task_id)
        
        if not task:
            return jsonify({
                'success': False,
                'error': 'Task not found'
            }), 404
        
        return jsonify({
            'success': True,
            'task': task
        })
        
    except Exception as e:
        logger.error(f"Task status error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/batch/check', methods=['POST'])
def batch_check():
    """
    فحص متعدد للحسابات (غير متزامن)
    """
    try:
        data = request.get_json()
        
        if not data or 'accounts' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing required field: accounts'
            }), 400
        
        accounts = data['accounts']
        check_type = data.get('type', 'stalker')  # stalker or xtream
        
        if not accounts or len(accounts) == 0:
            return jsonify({
                'success': False,
                'error': 'No accounts provided'
            }), 400
        
        # الحد الأقصى للحسابات
        accounts = accounts[:50]
        
        # إنشاء مهمة جديدة
        task_id = task_manager.create_task('batch_check', len(accounts))
        
        # تشغيل المهمة في thread منفصل
        def run_batch_check():
            task_manager.update_task(task_id, status='running')
            
            results = []
            for i, account in enumerate(accounts):
                try:
                    if check_type == 'stalker':
                        # فحص Stalker
                        stalker = StalkerPortalAPI(
                            account['host'], 
                            account['mac'],
                            account.get('portal_path', '/stalker_portal/server/load.php')
                        )
                        
                        if stalker.handshake():
                            profile = stalker.get_profile()
                            account_info = stalker.get_account_info()
                            
                            results.append({
                                'account': account,
                                'success': True,
                                'profile': profile,
                                'account_info': account_info
                            })
                        else:
                            results.append({
                                'account': account,
                                'success': False,
                                'error': 'Handshake failed'
                            })
                    
                    elif check_type == 'xtream':
                        # فحص Xtream
                        xtream = XtreamCodesAPI(
                            account['host'],
                            account['username'],
                            account['password']
                        )
                        
                        auth_result = xtream.authenticate()
                        results.append({
                            'account': account,
                            'success': auth_result['success'],
                            'user_info': auth_result.get('user_info'),
                            'server_info': auth_result.get('server_info'),
                            'error': auth_result.get('error')
                        })
                    
                except Exception as e:
                    results.append({
                        'account': account,
                        'success': False,
                        'error': str(e)
                    })
                
                # تحديث التقدم
                progress = ((i + 1) / len(accounts)) * 100
                task_manager.update_task(task_id, progress=progress)
            
            # تحديث النتائج النهائية
            task_manager.update_task(
                task_id, 
                status='completed', 
                progress=100,
                results=results
            )
        
        thread = threading.Thread(target=run_batch_check)
        thread.start()
        
        return jsonify({
            'success': True,
            'task_id': task_id,
            'message': 'Batch check started',
            'total_accounts': len(accounts)
        })
        
    except Exception as e:
        logger.error(f"Batch check error: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

# Error handlers
@app.errorhandler(404)
def not_found(error):
    return jsonify({
        'success': False,
        'error': 'Endpoint not found'
    }), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({
        'success': False,
        'error': 'Internal server error'
    }), 500

# تنظيف دوري للمهام القديمة
def cleanup_tasks():
    while True:
        time.sleep(3600)  # كل ساعة
        task_manager.cleanup_old_tasks()

# بدء thread التنظيف
cleanup_thread = threading.Thread(target=cleanup_tasks, daemon=True)
cleanup_thread.start()

if __name__ == '__main__':
    logger.info("Starting IPTV API Server...")
    app.run(host='0.0.0.0', port=5000, debug=False, threaded=True)