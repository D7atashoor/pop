#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IPTV Flask API - واجهة REST API لفحص IPTV
Flask REST API interface for IPTV checking
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import threading
import time
from iptv_api_extracted import IPTVAPIServer
from typing import Dict, List

app = Flask(__name__)
CORS(app)  # تمكين CORS لجميع الطلبات

# إنشاء مثيل من API
iptv_api = IPTVAPIServer()

# متغيرات لتتبع المهام
active_tasks = {}
task_counter = 0

class TaskManager:
    """إدارة المهام المتزامنة"""
    
    def __init__(self):
        self.tasks = {}
        self.counter = 0
    
    def create_task(self, task_type: str, total_items: int) -> str:
        """إنشاء مهمة جديدة"""
        self.counter += 1
        task_id = f"task_{self.counter}"
        
        self.tasks[task_id] = {
            'id': task_id,
            'type': task_type,
            'status': 'running',
            'progress': 0,
            'total': total_items,
            'current': 0,
            'results': [],
            'errors': [],
            'start_time': time.time(),
            'end_time': None
        }
        
        return task_id
    
    def update_task(self, task_id: str, current: int, result: Dict = None, error: str = None):
        """تحديث تقدم المهمة"""
        if task_id in self.tasks:
            task = self.tasks[task_id]
            task['current'] = current
            task['progress'] = (current / task['total']) * 100
            
            if result:
                task['results'].append(result)
            
            if error:
                task['errors'].append(error)
    
    def complete_task(self, task_id: str):
        """إنهاء المهمة"""
        if task_id in self.tasks:
            self.tasks[task_id]['status'] = 'completed'
            self.tasks[task_id]['end_time'] = time.time()
    
    def get_task(self, task_id: str) -> Dict:
        """الحصول على معلومات المهمة"""
        return self.tasks.get(task_id, {})

task_manager = TaskManager()

@app.route('/', methods=['GET'])
def home():
    """الصفحة الرئيسية - معلومات API"""
    return jsonify({
        'service': 'IPTV Checker API',
        'version': '1.0',
        'description': 'API لفحص واختبار بيانات اعتماد IPTV',
        'endpoints': {
            'check_account': '/api/check',
            'check_bulk': '/api/bulk-check',
            'get_task_status': '/api/task/<task_id>',
            'server_info': '/api/server-info'
        }
    })

@app.route('/api/check', methods=['POST'])
def check_single_account():
    """
    فحص حساب واحد
    POST /api/check
    Body: {
        "host": "server.com:8080",
        "username": "test_user",
        "password": "test_pass"
    }
    """
    try:
        data = request.get_json()
        
        if not data or not all(k in data for k in ('host', 'username', 'password')):
            return jsonify({
                'success': False,
                'error': 'Missing required fields: host, username, password'
            }), 400
        
        result = iptv_api.check_single_account(
            host=data['host'],
            username=data['username'],
            password=data['password']
        )
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'Internal server error: {str(e)}'
        }), 500

@app.route('/api/bulk-check', methods=['POST'])
def bulk_check_accounts():
    """
    فحص متعدد غير متزامن
    POST /api/bulk-check
    Body: {
        "host": "server.com:8080",
        "credentials": [
            "user1:pass1",
            "user2:pass2",
            "user3:pass3"
        ]
    }
    """
    try:
        data = request.get_json()
        
        if not data or not all(k in data for k in ('host', 'credentials')):
            return jsonify({
                'success': False,
                'error': 'Missing required fields: host, credentials'
            }), 400
        
        if not isinstance(data['credentials'], list):
            return jsonify({
                'success': False,
                'error': 'credentials must be a list'
            }), 400
        
        # إنشاء مهمة جديدة
        task_id = task_manager.create_task('bulk_check', len(data['credentials']))
        
        # تشغيل المهمة في thread منفصل
        thread = threading.Thread(
            target=_process_bulk_check,
            args=(task_id, data['host'], data['credentials'])
        )
        thread.start()
        
        return jsonify({
            'success': True,
            'task_id': task_id,
            'status': 'started',
            'message': 'Bulk check started. Use task_id to monitor progress.'
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'Internal server error: {str(e)}'
        }), 500

def _process_bulk_check(task_id: str, host: str, credentials: List[str]):
    """معالجة الفحص المتعدد في الخلفية"""
    try:
        def progress_callback(current, total, result):
            task_manager.update_task(task_id, current, result)
        
        # تحويل credentials إلى تنسيق مطلوب
        credentials_list = []
        for cred in credentials:
            is_valid, username, password = iptv_api.validate_combo_format(cred)
            if is_valid:
                credentials_list.append((host, username, password))
        
        # تشغيل الفحص
        results = iptv_api.checker.bulk_check(credentials_list, progress_callback)
        
        # تحديث النتائج النهائية
        task = task_manager.get_task(task_id)
        task['final_results'] = {
            'total_checked': len(results),
            'valid_accounts': len([r for r in results if r['valid']]),
            'invalid_accounts': len([r for r in results if not r['valid']]),
            'accounts': [r for r in results if r['valid']]
        }
        
        task_manager.complete_task(task_id)
        
    except Exception as e:
        task = task_manager.get_task(task_id)
        task['status'] = 'error'
        task['error'] = str(e)
        task['end_time'] = time.time()

@app.route('/api/task/<task_id>', methods=['GET'])
def get_task_status(task_id: str):
    """
    الحصول على حالة المهمة
    GET /api/task/{task_id}
    """
    task = task_manager.get_task(task_id)
    
    if not task:
        return jsonify({
            'success': False,
            'error': 'Task not found'
        }), 404
    
    # حساب الوقت المنقضي
    elapsed_time = time.time() - task['start_time']
    if task['end_time']:
        total_time = task['end_time'] - task['start_time']
    else:
        total_time = elapsed_time
    
    response = {
        'success': True,
        'task': {
            'id': task['id'],
            'type': task['type'],
            'status': task['status'],
            'progress': round(task['progress'], 2),
            'current': task['current'],
            'total': task['total'],
            'elapsed_time': round(elapsed_time, 2),
            'total_time': round(total_time, 2) if task['end_time'] else None
        }
    }
    
    # إضافة النتائج النهائية إذا اكتملت المهمة
    if task['status'] == 'completed' and 'final_results' in task:
        response['task']['results'] = task['final_results']
    
    return jsonify(response)

@app.route('/api/server-info', methods=['POST'])
def get_server_info():
    """
    الحصول على معلومات الخادم
    POST /api/server-info
    Body: {
        "host": "server.com:8080",
        "username": "test_user", 
        "password": "test_pass"
    }
    """
    try:
        data = request.get_json()
        
        if not data or not all(k in data for k in ('host', 'username', 'password')):
            return jsonify({
                'success': False,
                'error': 'Missing required fields: host, username, password'
            }), 400
        
        # فحص الحساب أولاً
        result = iptv_api.checker.check_credentials(
            data['host'], data['username'], data['password']
        )
        
        if not result['valid']:
            return jsonify({
                'success': False,
                'error': 'Invalid credentials',
                'details': result['error']
            }), 401
        
        # الحصول على معلومات إضافية
        counts = iptv_api.checker.get_channels_count(
            data['host'], data['username'], data['password']
        )
        categories = iptv_api.checker.get_categories(
            data['host'], data['username'], data['password']
        )
        
        return jsonify({
            'success': True,
            'data': {
                'account_info': result['account_info'],
                'content_counts': counts,
                'categories': categories,
                'server_health': 'active'
            }
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'Internal server error: {str(e)}'
        }), 500

@app.route('/api/validate-combo', methods=['POST'])
def validate_combo():
    """
    التحقق من صيغة combo
    POST /api/validate-combo
    Body: {
        "combo": "username:password"
    }
    """
    try:
        data = request.get_json()
        
        if not data or 'combo' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing combo field'
            }), 400
        
        is_valid, username, password = iptv_api.validate_combo_format(data['combo'])
        
        return jsonify({
            'success': True,
            'data': {
                'valid_format': is_valid,
                'username': username if is_valid else None,
                'password': password if is_valid else None
            }
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': f'Internal server error: {str(e)}'
        }), 500

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

if __name__ == '__main__':
    print("🚀 IPTV Checker API Starting...")
    print("📋 Available endpoints:")
    print("   POST /api/check - فحص حساب واحد")
    print("   POST /api/bulk-check - فحص متعدد")
    print("   GET  /api/task/<id> - حالة المهمة")
    print("   POST /api/server-info - معلومات الخادم")
    print("   POST /api/validate-combo - تحقق من combo")
    print("\n🌐 Server running on: http://localhost:5000")
    
    app.run(host='0.0.0.0', port=5000, debug=True)