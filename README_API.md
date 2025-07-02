# IPTV API Documentation - دليل استخدام API

## 📋 نظرة عامة

تم استخراج هذا API من سكريبت Ali Premium لتوفير واجهة نظيفة وآمنة لفحص بيانات اعتماد IPTV. يوفر API وظائف متكاملة لفحص الحسابات والحصول على معلومات الخوادم.

## 🚀 التشغيل السريع

### 1. تثبيت المتطلبات
```bash
pip install -r requirements.txt
```

### 2. تشغيل الخادم
```bash
python iptv_flask_api.py
```

### 3. اختبار API
```bash
curl http://localhost:5000/
```

## 📚 واجهات API المتاحة

### 🔍 فحص حساب واحد
**POST** `/api/check`

```json
{
  "host": "server.com:8080",
  "username": "test_user",
  "password": "test_pass"
}
```

**الاستجابة:**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "account_info": {
      "username": "test_user",
      "status": "Active",
      "expiry_date": "2024-12-31 23:59:59",
      "days_remaining": 45,
      "channels_count": {
        "live_channels": 1500,
        "movies": 800,
        "series": 200
      },
      "m3u_url": "http://server.com:8080/get.php?username=test_user&password=test_pass&type=m3u_plus"
    }
  }
}
```

### 📋 فحص متعدد (غير متزامن)
**POST** `/api/bulk-check`

```json
{
  "host": "server.com:8080",
  "credentials": [
    "user1:pass1",
    "user2:pass2",
    "user3:pass3"
  ]
}
```

**الاستجابة:**
```json
{
  "success": true,
  "task_id": "task_1",
  "status": "started",
  "message": "Bulk check started. Use task_id to monitor progress."
}
```

### ⏳ مراقبة تقدم المهمة
**GET** `/api/task/{task_id}`

**الاستجابة:**
```json
{
  "success": true,
  "task": {
    "id": "task_1",
    "status": "completed",
    "progress": 100.0,
    "current": 3,
    "total": 3,
    "results": {
      "total_checked": 3,
      "valid_accounts": 2,
      "invalid_accounts": 1,
      "accounts": [...]
    }
  }
}
```

### 🖥️ معلومات الخادم
**POST** `/api/server-info`

```json
{
  "host": "server.com:8080",
  "username": "test_user",
  "password": "test_pass"
}
```

### 🔧 التحقق من صيغة Combo
**POST** `/api/validate-combo`

```json
{
  "combo": "username:password"
}
```

## 🔧 التكامل مع تطبيق Android

### 1. إضافة التبعيات في `build.gradle`
```gradle
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.9.0'
```

### 2. إنشاء نماذج البيانات

```kotlin
// AccountInfo.kt
data class AccountInfo(
    val username: String,
    val status: String,
    val expiry_date: String,
    val days_remaining: Int,
    val channels_count: ChannelsCount,
    val m3u_url: String
)

data class ChannelsCount(
    val live_channels: Int,
    val movies: Int,
    val series: Int
)

// CheckResponse.kt
data class CheckResponse(
    val success: Boolean,
    val data: CheckData?,
    val error: String?
)

data class CheckData(
    val valid: Boolean,
    val account_info: AccountInfo?,
    val error: String?
)
```

### 3. إنشاء واجهة Retrofit

```kotlin
// IPTVApiService.kt
interface IPTVApiService {
    @POST("api/check")
    suspend fun checkAccount(@Body request: CheckRequest): CheckResponse
    
    @POST("api/bulk-check")
    suspend fun bulkCheck(@Body request: BulkCheckRequest): BulkCheckResponse
    
    @GET("api/task/{taskId}")
    suspend fun getTaskStatus(@Path("taskId") taskId: String): TaskStatusResponse
    
    @POST("api/server-info")
    suspend fun getServerInfo(@Body request: CheckRequest): ServerInfoResponse
}

data class CheckRequest(
    val host: String,
    val username: String,
    val password: String
)

data class BulkCheckRequest(
    val host: String,
    val credentials: List<String>
)
```

### 4. إنشاء Repository

```kotlin
// IPTVRepository.kt
class IPTVRepository {
    private val apiService = RetrofitClient.iptvApiService
    
    suspend fun checkAccount(host: String, username: String, password: String): Result<CheckResponse> {
        return try {
            val request = CheckRequest(host, username, password)
            val response = apiService.checkAccount(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun startBulkCheck(host: String, credentials: List<String>): Result<BulkCheckResponse> {
        return try {
            val request = BulkCheckRequest(host, credentials)
            val response = apiService.bulkCheck(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTaskStatus(taskId: String): Result<TaskStatusResponse> {
        return try {
            val response = apiService.getTaskStatus(taskId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 5. استخدام في ViewModel

```kotlin
// MainViewModel.kt
class MainViewModel : ViewModel() {
    private val repository = IPTVRepository()
    
    private val _checkResult = MutableLiveData<CheckResponse>()
    val checkResult: LiveData<CheckResponse> = _checkResult
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    fun checkAccount(host: String, username: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            
            repository.checkAccount(host, username, password)
                .onSuccess { response ->
                    _checkResult.value = response
                }
                .onFailure { exception ->
                    // Handle error
                    Log.e("ViewModel", "Error checking account", exception)
                }
            
            _loading.value = false
        }
    }
    
    fun startBulkCheck(host: String, credentials: List<String>) {
        viewModelScope.launch {
            repository.startBulkCheck(host, credentials)
                .onSuccess { response ->
                    if (response.success) {
                        // Start monitoring task
                        monitorTask(response.task_id)
                    }
                }
                .onFailure { exception ->
                    Log.e("ViewModel", "Error starting bulk check", exception)
                }
        }
    }
    
    private fun monitorTask(taskId: String) {
        viewModelScope.launch {
            while (true) {
                repository.getTaskStatus(taskId)
                    .onSuccess { response ->
                        if (response.success) {
                            val task = response.task
                            when (task.status) {
                                "completed" -> {
                                    // Task completed
                                    break
                                }
                                "error" -> {
                                    // Task failed
                                    break
                                }
                                else -> {
                                    // Update progress UI
                                    delay(2000) // Wait 2 seconds
                                }
                            }
                        }
                    }
                    .onFailure { exception ->
                        Log.e("ViewModel", "Error getting task status", exception)
                        break
                    }
            }
        }
    }
}
```

### 6. استخدام في Activity/Fragment

```kotlin
// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupObservers() {
        viewModel.checkResult.observe(this) { response ->
            if (response.success && response.data?.valid == true) {
                val accountInfo = response.data.account_info
                showAccountInfo(accountInfo)
            } else {
                showError(response.error ?: "Unknown error")
            }
        }
        
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.checkButton.isEnabled = !isLoading
        }
    }
    
    private fun setupClickListeners() {
        binding.checkButton.setOnClickListener {
            val host = binding.hostEditText.text.toString()
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            
            if (host.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.checkAccount(host, username, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showAccountInfo(accountInfo: AccountInfo?) {
        accountInfo?.let { info ->
            binding.resultTextView.text = """
                Status: ${info.status}
                Expiry: ${info.expiry_date}
                Days Remaining: ${info.days_remaining}
                Channels: ${info.channels_count.live_channels}
                Movies: ${info.channels_count.movies}
                Series: ${info.channels_count.series}
            """.trimIndent()
        }
    }
    
    private fun showError(error: String) {
        binding.resultTextView.text = "Error: $error"
    }
}
```

## 🔒 اعتبارات الأمان

### 1. استخدام HTTPS في الإنتاج
```python
# للإنتاج، استخدم HTTPS
app.run(host='0.0.0.0', port=443, ssl_context='adhoc')
```

### 2. إضافة Authentication
```python
from functools import wraps

def require_auth(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        auth = request.headers.get('Authorization')
        if not auth or not verify_token(auth):
            return jsonify({'error': 'Unauthorized'}), 401
        return f(*args, **kwargs)
    return decorated_function

@app.route('/api/check', methods=['POST'])
@require_auth
def check_single_account():
    # ...
```

### 3. Rate Limiting
```python
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

limiter = Limiter(
    app,
    key_func=get_remote_address,
    default_limits=["100 per hour"]
)

@app.route('/api/check', methods=['POST'])
@limiter.limit("10 per minute")
def check_single_account():
    # ...
```

## 📊 مراقبة الأداء

### 1. إضافة Logging
```python
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.route('/api/check', methods=['POST'])
def check_single_account():
    start_time = time.time()
    # ... API logic ...
    end_time = time.time()
    logger.info(f"Check account took {end_time - start_time:.2f} seconds")
```

### 2. إضافة Metrics
```python
from prometheus_client import Counter, Histogram

REQUEST_COUNT = Counter('api_requests_total', 'Total API requests', ['method', 'endpoint'])
REQUEST_LATENCY = Histogram('api_request_duration_seconds', 'API request latency')

@app.before_request
def before_request():
    REQUEST_COUNT.labels(method=request.method, endpoint=request.endpoint).inc()
```

## 🚀 نشر الإنتاج

### 1. استخدام Gunicorn
```bash
pip install gunicorn
gunicorn -w 4 -b 0.0.0.0:5000 iptv_flask_api:app
```

### 2. Docker Deployment
```dockerfile
# Dockerfile
FROM python:3.9-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .
EXPOSE 5000

CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:5000", "iptv_flask_api:app"]
```

```bash
docker build -t iptv-api .
docker run -p 5000:5000 iptv-api
```

## ⚠️ تحذيرات قانونية

**هام جداً:** هذا API مُستخرج لأغراض تعليمية وتطويرية فقط. استخدام هذا API لفحص بيانات اعتماد IPTV غير مملوكة أو غير مصرح بها قد يكون غير قانوني في العديد من الولايات القضائية.

### المسؤوليات:
- **المطور غير مسؤول** عن أي استخدام غير قانوني
- **يجب الحصول على إذن** من أصحاب الخوادم قبل الفحص
- **احترام شروط الخدمة** لمقدمي خدمات IPTV
- **استخدام VPN** للحماية إذا لزم الأمر

## 📞 الدعم والمساعدة

للمساعدة في التكامل أو الاستخدام، يرجى:
1. مراجعة الأمثلة في `api_usage_examples.py`
2. فحص logs للأخطاء
3. التأكد من صحة المتطلبات المثبتة

## 📝 المساهمة

لتحسين هذا API:
1. Fork المشروع
2. إنشاء branch جديد للميزة
3. Commit التغييرات
4. Push إلى البranch
5. إنشاء Pull Request

---

**تذكير:** استخدم هذا API بمسؤولية واحترم القوانين المحلية والدولية.