/**
 * Android Integration Examples for IPTV API
 * أمثلة تكامل Android مع واجهة برمجة تطبيقات IPTV
 */

// Data Classes - فئات البيانات
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

data class PortalDiscoveryResult(
    val success: Boolean,
    val statusCode: Int,
    val endpoint: String,
    val portalType: String,
    val host: String,
    val fullUrl: String,
    val geoInfo: GeoInfo? = null
)

data class GeoInfo(
    val ip: String,
    val country: String,
    val countryCode: String,
    val city: String,
    val region: String,
    val isp: String,
    val continent: String,
    val countryFlag: String? = null
)

data class StalkerCheckResult(
    val success: Boolean,
    val host: String,
    val mac: String,
    val portalPath: String,
    val token: String?,
    val profile: Any?,
    val accountInfo: Any?,
    val channels: List<Channel>? = null,
    val channelsCount: Int = 0
)

data class XtreamCheckResult(
    val success: Boolean,
    val host: String,
    val username: String,
    val userInfo: UserInfo,
    val serverInfo: ServerInfo,
    val m3uUrl: String,
    val xmltvUrl: String
)

data class UserInfo(
    val username: String,
    val password: String,
    val message: String,
    val auth: Int,
    val status: String,
    val expDate: String,
    val isTrial: String,
    val activeCons: String,
    val createdAt: String,
    val maxConnections: String
)

data class ServerInfo(
    val url: String,
    val port: String,
    val httpsPort: String,
    val serverProtocol: String,
    val rtmpPort: String,
    val timezone: String,
    val timestampNow: Long,
    val timeNow: String
)

data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val tvgLogo: String? = null,
    val groupTitle: String? = null
)

data class MacCredentials(
    val mac: String,
    val deviceCredentials: DeviceCredentials
)

data class DeviceCredentials(
    val mac: String,
    val macEncoded: String,
    val serialNumber: String,
    val deviceId: String,
    val deviceId2: String,
    val signature: String,
    val stbType: String
)

// API Service Class - فئة خدمة API
class IPTVApiService(private val baseUrl: String = "http://localhost:5000") {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    /**
     * اكتشاف نوع البوابة
     */
    suspend fun discoverPortal(host: String): ApiResponse<PortalDiscoveryResult> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = JSONObject().apply {
                    put("host", host)
                }
                
                val response = makePostRequest("/api/discover", requestBody)
                val result = gson.fromJson(response, PortalDiscoveryResult::class.java)
                
                ApiResponse(success = true, data = result)
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    /**
     * فحص Stalker Portal
     */
    suspend fun checkStalker(
        host: String,
        mac: String,
        portalPath: String = "/stalker_portal/server/load.php",
        includeChannels: Boolean = false,
        includeVod: Boolean = false,
        includeSeries: Boolean = false
    ): ApiResponse<StalkerCheckResult> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = JSONObject().apply {
                    put("host", host)
                    put("mac", mac)
                    put("portal_path", portalPath)
                    put("include_channels", includeChannels)
                    put("include_vod", includeVod)
                    put("include_series", includeSeries)
                }
                
                val response = makePostRequest("/api/stalker/check", requestBody)
                val result = gson.fromJson(response, StalkerCheckResult::class.java)
                
                ApiResponse(success = true, data = result)
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    /**
     * فحص Xtream Codes
     */
    suspend fun checkXtream(
        host: String,
        username: String,
        password: String,
        includeContent: Boolean = false,
        includeFullContent: Boolean = false
    ): ApiResponse<XtreamCheckResult> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = JSONObject().apply {
                    put("host", host)
                    put("username", username)
                    put("password", password)
                    put("include_content", includeContent)
                    put("include_full_content", includeFullContent)
                }
                
                val response = makePostRequest("/api/xtream/check", requestBody)
                val result = gson.fromJson(response, XtreamCheckResult::class.java)
                
                ApiResponse(success = true, data = result)
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    /**
     * تحليل ملف M3U
     */
    suspend fun parseM3U(
        content: String? = null,
        url: String? = null
    ): ApiResponse<List<Channel>> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = JSONObject().apply {
                    content?.let { put("content", it) }
                    url?.let { put("url", it) }
                }
                
                val response = makePostRequest("/api/m3u/parse", requestBody)
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    val channelsArray = jsonResponse.getJSONArray("channels")
                    val channels = mutableListOf<Channel>()
                    
                    for (i in 0 until channelsArray.length()) {
                        val channelObj = channelsArray.getJSONObject(i)
                        val channel = Channel(
                            id = channelObj.optString("tvg-id", ""),
                            name = channelObj.getString("name"),
                            url = channelObj.getString("url"),
                            tvgId = channelObj.optString("tvg-id"),
                            tvgName = channelObj.optString("tvg-name"),
                            tvgLogo = channelObj.optString("tvg-logo"),
                            groupTitle = channelObj.optString("group-title")
                        )
                        channels.add(channel)
                    }
                    
                    ApiResponse(success = true, data = channels)
                } else {
                    ApiResponse(success = false, error = jsonResponse.optString("error"))
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    /**
     * توليد عنوان MAC
     */
    suspend fun generateMAC(
        prefix: String? = null,
        count: Int = 1
    ): ApiResponse<List<MacCredentials>> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = JSONObject().apply {
                    prefix?.let { put("prefix", it) }
                    put("count", count)
                }
                
                val response = makePostRequest("/api/utils/generate_mac", requestBody)
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    val macsData = if (count == 1) {
                        listOf(jsonResponse.getJSONObject("macs"))
                    } else {
                        val array = jsonResponse.getJSONArray("macs")
                        (0 until array.length()).map { array.getJSONObject(it) }
                    }
                    
                    val macs = macsData.map { macObj ->
                        val credsObj = macObj.getJSONObject("device_credentials")
                        MacCredentials(
                            mac = macObj.getString("mac"),
                            deviceCredentials = DeviceCredentials(
                                mac = credsObj.getString("mac"),
                                macEncoded = credsObj.getString("mac_encoded"),
                                serialNumber = credsObj.getString("serial_number"),
                                deviceId = credsObj.getString("device_id"),
                                deviceId2 = credsObj.getString("device_id2"),
                                signature = credsObj.getString("signature"),
                                stbType = credsObj.getString("stb_type")
                            )
                        )
                    }
                    
                    ApiResponse(success = true, data = macs)
                } else {
                    ApiResponse(success = false, error = jsonResponse.optString("error"))
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    /**
     * الحصول على معلومات الموقع الجغرافي
     */
    suspend fun getGeoLocation(ipOrHost: String): ApiResponse<GeoInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = JSONObject().apply {
                    put("ip_or_host", ipOrHost)
                }
                
                val response = makePostRequest("/api/utils/geo_location", requestBody)
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    val geoObj = jsonResponse.getJSONObject("geo_info")
                    val geoInfo = GeoInfo(
                        ip = geoObj.getString("ip"),
                        country = geoObj.getString("country"),
                        countryCode = geoObj.getString("country_code"),
                        city = geoObj.getString("city"),
                        region = geoObj.getString("region"),
                        isp = geoObj.getString("isp"),
                        continent = geoObj.getString("continent"),
                        countryFlag = geoObj.optString("country_flag")
                    )
                    
                    ApiResponse(success = true, data = geoInfo)
                } else {
                    ApiResponse(success = false, error = jsonResponse.optString("error"))
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    /**
     * فحص توفر رابط البث
     */
    suspend fun checkStreamAvailability(url: String, timeout: Int = 5): ApiResponse<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = JSONObject().apply {
                    put("url", url)
                    put("timeout", timeout)
                }
                
                val response = makePostRequest("/api/utils/check_stream", requestBody)
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    val available = jsonResponse.getBoolean("available")
                    ApiResponse(success = true, data = available)
                } else {
                    ApiResponse(success = false, error = jsonResponse.optString("error"))
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    /**
     * فحص متعدد للحسابات
     */
    suspend fun batchCheck(
        accounts: List<Map<String, String>>,
        type: String = "stalker"
    ): ApiResponse<String> {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = JSONObject().apply {
                    put("accounts", JSONArray(accounts))
                    put("type", type)
                }
                
                val response = makePostRequest("/api/batch/check", requestBody)
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    val taskId = jsonResponse.getString("task_id")
                    ApiResponse(success = true, data = taskId)
                } else {
                    ApiResponse(success = false, error = jsonResponse.optString("error"))
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    /**
     * الحصول على حالة المهمة
     */
    suspend fun getTaskStatus(taskId: String): ApiResponse<Any> {
        return withContext(Dispatchers.IO) {
            try {
                val response = makeGetRequest("/api/task/$taskId")
                val jsonResponse = JSONObject(response)
                
                if (jsonResponse.getBoolean("success")) {
                    val task = jsonResponse.getJSONObject("task")
                    ApiResponse(success = true, data = task)
                } else {
                    ApiResponse(success = false, error = jsonResponse.optString("error"))
                }
            } catch (e: Exception) {
                ApiResponse(success = false, error = e.message)
            }
        }
    }
    
    // Helper Methods - الطرق المساعدة
    
    private fun makePostRequest(endpoint: String, requestBody: JSONObject): String {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestBody.toString().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .post(body)
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            return response.body!!.string()
        }
    }
    
    private fun makeGetRequest(endpoint: String): String {
        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .get()
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }
            return response.body!!.string()
        }
    }
}

// Repository Pattern - نمط المستودع
class IPTVRepository(private val apiService: IPTVApiService) {
    
    suspend fun addStalkerSource(
        name: String,
        host: String,
        mac: String,
        portalPath: String = "/stalker_portal/server/load.php"
    ): Result<Source> {
        return try {
            // اكتشاف البوابة أولاً
            val discoveryResult = apiService.discoverPortal(host)
            if (!discoveryResult.success) {
                return Result.failure(Exception(discoveryResult.error))
            }
            
            // فحص الاتصال
            val checkResult = apiService.checkStalker(host, mac, portalPath)
            if (!checkResult.success) {
                return Result.failure(Exception(checkResult.error))
            }
            
            // إنشاء مصدر جديد
            val source = Source(
                id = 0, // سيتم تعيينه من قاعدة البيانات
                name = name,
                type = SourceType.STALKER_PORTAL,
                host = host,
                mac = mac,
                portalPath = portalPath,
                isActive = true,
                lastChecked = System.currentTimeMillis()
            )
            
            Result.success(source)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addXtreamSource(
        name: String,
        host: String,
        username: String,
        password: String
    ): Result<Source> {
        return try {
            // فحص الاتصال
            val checkResult = apiService.checkXtream(host, username, password)
            if (!checkResult.success) {
                return Result.failure(Exception(checkResult.error))
            }
            
            // إنشاء مصدر جديد
            val source = Source(
                id = 0,
                name = name,
                type = SourceType.XTREAM_CODES,
                host = host,
                username = username,
                password = password,
                isActive = true,
                lastChecked = System.currentTimeMillis()
            )
            
            Result.success(source)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loadChannelsFromM3U(url: String): Result<List<Channel>> {
        return try {
            val result = apiService.parseM3U(url = url)
            if (result.success && result.data != null) {
                Result.success(result.data)
            } else {
                Result.failure(Exception(result.error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun generateMACAddress(prefix: String? = null): Result<MacCredentials> {
        return try {
            val result = apiService.generateMAC(prefix, 1)
            if (result.success && result.data != null && result.data.isNotEmpty()) {
                Result.success(result.data.first())
            } else {
                Result.failure(Exception(result.error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Usage Example in ViewModel - مثال الاستخدام في ViewModel
class AddSourceViewModel(
    private val repository: IPTVRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddSourceUiState())
    val uiState: StateFlow<AddSourceUiState> = _uiState.asStateFlow()
    
    fun discoverPortal(host: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = repository.apiService.discoverPortal(host)
                if (result.success && result.data != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            discoveredPortal = result.data,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.error
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun generateMAC() {
        viewModelScope.launch {
            try {
                val result = repository.generateMACAddress()
                result.fold(
                    onSuccess = { macCreds ->
                        _uiState.update { 
                            it.copy(generatedMAC = macCreds.mac)
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update { 
                            it.copy(error = throwable.message)
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun addStalkerSource(name: String, host: String, mac: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = repository.addStalkerSource(name, host, mac)
                result.fold(
                    onSuccess = { source ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isSourceAdded = true,
                                error = null
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = throwable.message
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}

data class AddSourceUiState(
    val isLoading: Boolean = false,
    val discoveredPortal: PortalDiscoveryResult? = null,
    val generatedMAC: String? = null,
    val isSourceAdded: Boolean = false,
    val error: String? = null
)