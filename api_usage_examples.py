#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
أمثلة استخدام IPTV API
Usage examples for IPTV API
"""

import requests
import json
import time

# إعدادات API
API_BASE_URL = "http://localhost:5000"

def check_single_account_example():
    """مثال على فحص حساب واحد"""
    print("🔍 فحص حساب واحد...")
    
    url = f"{API_BASE_URL}/api/check"
    data = {
        "host": "example.com:8080",
        "username": "test_user",
        "password": "test_pass"
    }
    
    try:
        response = requests.post(url, json=data)
        result = response.json()
        
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(result, indent=2, ensure_ascii=False)}")
        
        if result.get('success') and result.get('data', {}).get('valid'):
            account_info = result['data']['account_info']
            print(f"\n✅ حساب صالح!")
            print(f"الحالة: {account_info.get('status')}")
            print(f"تاريخ الانتهاء: {account_info.get('expiry_date')}")
            print(f"الأيام المتبقية: {account_info.get('days_remaining')}")
        else:
            print(f"\n❌ حساب غير صالح: {result.get('data', {}).get('error')}")
            
    except Exception as e:
        print(f"خطأ: {e}")

def bulk_check_example():
    """مثال على الفحص المتعدد"""
    print("\n📋 فحص متعدد...")
    
    # بدء الفحص المتعدد
    url = f"{API_BASE_URL}/api/bulk-check"
    data = {
        "host": "example.com:8080",
        "credentials": [
            "user1:pass1",
            "user2:pass2", 
            "user3:pass3",
            "user4:pass4",
            "user5:pass5"
        ]
    }
    
    try:
        response = requests.post(url, json=data)
        result = response.json()
        
        if result.get('success'):
            task_id = result['task_id']
            print(f"✅ بدأت المهمة: {task_id}")
            
            # مراقبة تقدم المهمة
            monitor_task_progress(task_id)
        else:
            print(f"❌ خطأ في بدء المهمة: {result.get('error')}")
            
    except Exception as e:
        print(f"خطأ: {e}")

def monitor_task_progress(task_id):
    """مراقبة تقدم المهمة"""
    print(f"\n⏳ مراقبة المهمة {task_id}...")
    
    url = f"{API_BASE_URL}/api/task/{task_id}"
    
    while True:
        try:
            response = requests.get(url)
            result = response.json()
            
            if result.get('success'):
                task = result['task']
                status = task['status']
                progress = task['progress']
                current = task['current']
                total = task['total']
                
                print(f"\r📊 التقدم: {progress:.1f}% ({current}/{total}) - الحالة: {status}", end="")
                
                if status == 'completed':
                    print(f"\n✅ اكتملت المهمة!")
                    
                    if 'results' in task:
                        results = task['results']
                        print(f"📊 النتائج:")
                        print(f"   إجمالي المفحوص: {results['total_checked']}")
                        print(f"   الصالح: {results['valid_accounts']}")
                        print(f"   غير الصالح: {results['invalid_accounts']}")
                        
                        # عرض الحسابات الصالحة
                        if results['accounts']:
                            print(f"\n✅ الحسابات الصالحة:")
                            for i, account in enumerate(results['accounts'][:5], 1):
                                print(f"   {i}. {account['username']}:{account['password']}")
                                print(f"      الحالة: {account['status']}")
                                print(f"      الانتهاء: {account['expiry_date']}")
                                if account.get('channels_count'):
                                    counts = account['channels_count']
                                    print(f"      المحتوى: {counts['live_channels']} قناة، {counts['movies']} فيلم")
                    break
                    
                elif status == 'error':
                    print(f"\n❌ خطأ في المهمة: {task.get('error')}")
                    break
                
                time.sleep(2)  # انتظار ثانيتين قبل التحقق مرة أخرى
                
            else:
                print(f"\n❌ خطأ في الحصول على حالة المهمة: {result.get('error')}")
                break
                
        except Exception as e:
            print(f"\n❌ خطأ: {e}")
            break

def get_server_info_example():
    """مثال على الحصول على معلومات الخادم"""
    print("\n🖥️ معلومات الخادم...")
    
    url = f"{API_BASE_URL}/api/server-info"
    data = {
        "host": "example.com:8080",
        "username": "test_user",
        "password": "test_pass"
    }
    
    try:
        response = requests.post(url, json=data)
        result = response.json()
        
        print(f"Status Code: {response.status_code}")
        
        if result.get('success'):
            data = result['data']
            account_info = data['account_info']
            content_counts = data['content_counts']
            categories = data['categories']
            
            print(f"✅ معلومات الخادم:")
            print(f"URL الخادم: {account_info['server_url']}")
            print(f"المنطقة الزمنية: {account_info['timezone']}")
            print(f"رابط M3U: {account_info['m3u_url']}")
            print(f"\n📺 المحتوى:")
            print(f"القنوات المباشرة: {content_counts['live_channels']}")
            print(f"الأفلام: {content_counts['movies']}")
            print(f"المسلسلات: {content_counts['series']}")
            print(f"\n📂 الفئات ({len(categories)}):")
            for i, category in enumerate(categories[:10], 1):
                print(f"   {i}. {category}")
        else:
            print(f"❌ خطأ: {result.get('error')}")
            
    except Exception as e:
        print(f"خطأ: {e}")

def validate_combo_example():
    """مثال على التحقق من صيغة combo"""
    print("\n🔧 التحقق من صيغة combo...")
    
    url = f"{API_BASE_URL}/api/validate-combo"
    
    test_combos = [
        "user123:pass456",  # صحيح
        "user:pass:extra",  # خطأ - أكثر من :
        "useronly",         # خطأ - بدون :
        "user:",            # خطأ - بدون password
        ":password"         # خطأ - بدون username
    ]
    
    for combo in test_combos:
        data = {"combo": combo}
        
        try:
            response = requests.post(url, json=data)
            result = response.json()
            
            if result.get('success'):
                combo_data = result['data']
                valid = combo_data['valid_format']
                username = combo_data['username']
                password = combo_data['password']
                
                if valid:
                    print(f"✅ '{combo}' -> صالح (user: {username}, pass: {password})")
                else:
                    print(f"❌ '{combo}' -> غير صالح")
            else:
                print(f"❌ خطأ في التحقق من '{combo}': {result.get('error')}")
                
        except Exception as e:
            print(f"❌ خطأ: {e}")

def android_integration_example():
    """مثال للتكامل مع تطبيق Android"""
    print("\n📱 مثال للتكامل مع Android...")
    
    class AndroidIPTVClient:
        def __init__(self, api_base_url):
            self.api_base_url = api_base_url
            
        def check_user_account(self, host, username, password):
            """فحص حساب المستخدم"""
            url = f"{self.api_base_url}/api/check"
            data = {
                "host": host,
                "username": username,
                "password": password
            }
            
            try:
                response = requests.post(url, json=data, timeout=30)
                return response.json()
            except Exception as e:
                return {
                    "success": False,
                    "error": f"Network error: {str(e)}"
                }
        
        def start_bulk_check(self, host, credentials_list):
            """بدء فحص متعدد"""
            url = f"{self.api_base_url}/api/bulk-check"
            data = {
                "host": host,
                "credentials": credentials_list
            }
            
            try:
                response = requests.post(url, json=data, timeout=30)
                return response.json()
            except Exception as e:
                return {
                    "success": False,
                    "error": f"Network error: {str(e)}"
                }
        
        def get_task_status(self, task_id):
            """الحصول على حالة المهمة"""
            url = f"{self.api_base_url}/api/task/{task_id}"
            
            try:
                response = requests.get(url, timeout=30)
                return response.json()
            except Exception as e:
                return {
                    "success": False,
                    "error": f"Network error: {str(e)}"
                }
    
    # استخدام العميل
    client = AndroidIPTVClient(API_BASE_URL)
    
    # فحص حساب
    result = client.check_user_account("example.com:8080", "test_user", "test_pass")
    print(f"نتيجة فحص الحساب: {result.get('success')}")
    
    # بدء فحص متعدد
    credentials = ["user1:pass1", "user2:pass2"]
    bulk_result = client.start_bulk_check("example.com:8080", credentials)
    
    if bulk_result.get('success'):
        task_id = bulk_result['task_id']
        print(f"بدأت المهمة: {task_id}")
        
        # مراقبة التقدم
        while True:
            status_result = client.get_task_status(task_id)
            if status_result.get('success'):
                task = status_result['task']
                if task['status'] == 'completed':
                    print(f"اكتملت المهمة: {task.get('results', {})}")
                    break
                elif task['status'] == 'error':
                    print(f"خطأ في المهمة: {task.get('error')}")
                    break
                else:
                    print(f"التقدم: {task['progress']:.1f}%")
                    time.sleep(2)
            else:
                print(f"خطأ في الحصول على الحالة: {status_result.get('error')}")
                break

if __name__ == "__main__":
    print("🎯 أمثلة استخدام IPTV API")
    print("=" * 50)
    
    # تشغيل الأمثلة
    try:
        check_single_account_example()
        time.sleep(1)
        
        bulk_check_example()
        time.sleep(1)
        
        get_server_info_example()
        time.sleep(1)
        
        validate_combo_example()
        time.sleep(1)
        
        android_integration_example()
        
    except KeyboardInterrupt:
        print("\n\n⏹️ تم إيقاف الأمثلة بواسطة المستخدم")
    except Exception as e:
        print(f"\n❌ خطأ عام: {e}")
    
    print("\n✅ انتهت الأمثلة")