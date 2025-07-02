package com.iptv.player.data.network

import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import android.util.Log
import org.json.JSONObject
import java.net.InetAddress
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import java.text.SimpleDateFormat
import java.util.Locale

@Singleton
class SourceValidationService @Inject constructor(
    private val stalkerService: StalkerService,
    private val xtreamService: XtreamService,
    private val m3uParser: M3UParser
) {
    
    companion object {
        private const val TAG = "SourceValidationService"
        
        // Portal types المتقدمة من STB7PRO
        private val ADVANCED_PORTAL_ENDPOINTS = listOf(
            "/stalker_portal/server/load.php",
            "/stalker_portal/c/portal.php",
            "/stalker_portal/stb/portal.php",
            "/portal.php",
            "/server/load.php",
            "/c/portal.php",
            "/c/server/load.php",
            "/cp/server/load.php",
            "/cp/portal.php",
            "/p/portal.php",
            "/k/portal.php",
            "/rmxportal/portal.php",
            "/cmdforex/portal.php",
            "/portalstb/portal.php",
            "/portalstb.php",
            "/magLoad.php",
            "/magLoad/portal.php",
            "/maglove/portal.php",
            "/client/portal.php",
            "/magportal/portal.php",
            "/magaccess/portal.php",
            "/powerfull/portal.php",
            "/portalmega.php",
            "/portalmega/portal.php",
            "/ministra/portal.php",
            "/korisnici/server/load.php",
            "/ghandi_portal/server/load.php",
            "/blowportal/portal.php",
            "/extraportal.php",
            "/emu2/server/load.php",
            "/emu/server/load.php",
            "/tek/server/load.php",
            "/mag/portal.php",
            "/Link_OK.php",
            "/Link_OK/portal.php",
            "/bs.mag.portal.php",
            "/bStream/portal.php",
            "/bStream/server/load.php",
            "/delko/portal.php",
            "/delko/server/load.php",
            "/aurora/portal.php",
            "/edge.php",
            "/portalcc.php"
        )
        
        // User Agents متقدمة
        private val ADVANCED_USER_AGENTS = mapOf(
            "MAG200" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 2 rev: 250 Safari/533.3",
            "MAG250" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG250 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "MAG254" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG254 stbapp ver: 4 rev: 2721 Mobile Safari/533.3",
            "MAG270" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG270 stbapp ver: 4 rev: 2721 Mobile Safari/533.3",
            "MAG350" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG350 stbapp ver: 4 rev: 2721 Mobile Safari/533.3",
            "AndroidTV" to "Dalvik/2.1.0 (Linux; U; Android 9; ADT-2 Build/PTT5.181126.002)",
            "AppleTV5" to "AppleTV6,2/11.1",
            "AppleTV6" to "AppleTV11,1/11.1",
            "FireTV" to "Mozilla/5.0 (Linux; Android 5.1; AFTS Build/LMY47O) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/41.99900.2250.0242 Safari/537.36",
            "PlayStation" to "Mozilla/5.0 (PlayStation; PlayStation 5/2.26) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Safari/605.1.15",
            "Xbox" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; Xbox; Xbox Series X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36 Edge/20.02",
            "Roku" to "Roku/DVP-9.10 (559.10E04111A)",
            "CloudFlare" to "Mozilla/5.0 (compatible; CloudFlare-AlwaysOnline/1.0; +https://www.cloudflare.com/always-online) AppleWebKit/534.34 (KHTML, like Gecko) MAG200 stbapp ver: 2 rev: 250 Safari/534.34"
        )
        
        // Timezone mappings
        private val TIMEZONE_COUNTRIES = mapOf(
            "Europe/London" to "🇬🇧 United Kingdom",
            "Europe/Paris" to "🇫🇷 France", 
            "Europe/Berlin" to "🇩🇪 Germany",
            "Europe/Istanbul" to "🇹🇷 Turkey",
            "Europe/Moscow" to "🇷🇺 Russia",
            "America/New_York" to "🇺🇸 United States",
            "America/Los_Angeles" to "🇺🇸 United States",
            "Asia/Dubai" to "🇦🇪 UAE",
            "Asia/Riyadh" to "🇸🇦 Saudi Arabia",
            "Asia/Kuwait" to "🇰🇼 Kuwait",
            "Asia/Baghdad" to "🇮🇶 Iraq",
            "Asia/Tehran" to "🇮🇷 Iran",
            "Asia/Tokyo" to "🇯🇵 Japan",
            "Asia/Shanghai" to "🇨🇳 China",
            "Australia/Sydney" to "🇦🇺 Australia"
        )
    }
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    /**
     * التحقق الشامل من صحة المصدر - محسن
     */
    suspend fun validateSource(
        sourceType: SourceType,
        url: String,
        username: String? = null,
        password: String? = null,
        macAddress: String? = null,
        portalPath: String? = null
    ): SourceValidationResult = withContext(Dispatchers.IO) {
        
        Log.d(TAG, "بدء التحقق المحسن من المصدر: $sourceType - $url")
        
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            // التحقق من صحة الرابط
            if (!isValidUrl(url)) {
                issues.add("رابط غير صحيح")
                Log.e(TAG, "رابط غير صحيح: $url")
                return@withContext SourceValidationResult(
                    isValid = false,
                    sourceType = null,
                    issues = issues
                )
            }
            
            Log.d(TAG, "الرابط صحيح، جاري الحصول على معلومات الخادم المحسنة...")
            
            // الحصول على معلومات الخادم المحسنة
            val serverInfo = getEnhancedServerInfo(url)
            Log.d(TAG, "معلومات الخادم المحسنة: $serverInfo")
            
            // التحقق حسب نوع المصدر
            when (sourceType) {
                SourceType.STALKER -> {
                    Log.d(TAG, "التحقق المحسن من Stalker Portal...")
                    if (macAddress.isNullOrEmpty()) {
                        issues.add("عنوان MAC مطلوب للـ Stalker Portal")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    if (!stalkerService.isValidMAC(macAddress)) {
                        issues.add("عنوان MAC غير صحيح")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    return@withContext validateStalkerSourceAdvanced(url, macAddress, portalPath, serverInfo)
                }
                
                SourceType.XTREAM -> {
                    Log.d(TAG, "التحقق المحسن من Xtream Codes...")
                    if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
                        issues.add("اسم المستخدم وكلمة المرور مطلوبان للـ Xtream Codes")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    return@withContext validateXtreamSourceAdvanced(url, username, password, serverInfo)
                }
                
                SourceType.M3U -> {
                    Log.d(TAG, "التحقق المحسن من M3U...")
                    return@withContext validateM3USourceAdvanced(url, serverInfo)
                }
                
                SourceType.MAC_PORTAL -> {
                    Log.d(TAG, "التحقق المحسن من MAC Portal...")
                    if (macAddress.isNullOrEmpty()) {
                        issues.add("عنوان MAC مطلوب للـ MAC Portal")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    return@withContext validateMacPortalSourceAdvanced(url, macAddress, portalPath, serverInfo)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في التحقق من المصدر", e)
            issues.add("خطأ في التحقق من المصدر: ${e.localizedMessage ?: e.message}")
            return@withContext SourceValidationResult(
                isValid = false,
                sourceType = sourceType,
                issues = issues
            )
        }
    }
    
    /**
     * اكتشاف نوع المصدر تلقائياً - محسن
     */
    suspend fun detectSourceType(url: String): SourceType? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "اكتشاف نوع المصدر المحسن: $url")
            
            // فحص رابط M3U
            if (isM3UUrl(url)) {
                Log.d(TAG, "تم اكتشاف نوع M3U")
                return@withContext SourceType.M3U
            }
            
            // فحص Xtream Codes
            if (isXtreamUrl(url)) {
                Log.d(TAG, "تم اكتشاف نوع Xtream")
                return@withContext SourceType.XTREAM
            }
            
            // فحص Stalker Portal المحسن
            if (isStalkerUrl(url)) {
                Log.d(TAG, "تم اكتشاف نوع Stalker")
                return@withContext SourceType.STALKER
            }
            
            // فحص MAC Portal
            if (isMacPortalUrl(url)) {
                Log.d(TAG, "تم اكتشاف نوع MAC Portal")
                return@withContext SourceType.MAC_PORTAL
            }
            
            // محاولة فحص المحتوى
            val contentType = detectContentType(url)
            Log.d(TAG, "نوع المحتوى المكتشف: $contentType")
            return@withContext contentType
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في اكتشاف نوع المصدر", e)
            return@withContext null
        }
    }
    
    /**
     * اكتشاف نوع المحتوى من خلال فحص الاستجابة
     */
    private suspend fun detectContentType(url: String): SourceType? {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .addHeader("User-Agent", ADVANCED_USER_AGENTS["MAG254"]!!)
                .build()
            
            val response = withTimeoutOrNull(5000) {
                httpClient.newCall(request).execute()
            }
            
            response?.use {
                val contentType = it.header("Content-Type")?.lowercase()
                when {
                    contentType?.contains("application/vnd.apple.mpegurl") == true ||
                    contentType?.contains("application/x-mpegURL") == true ||
                    contentType?.contains("audio/x-mpegurl") == true -> SourceType.M3U
                    
                    it.code == 512 || it.code == 401 -> SourceType.STALKER // Stalker responses
                    
                    else -> SourceType.M3U // Default fallback
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في فحص نوع المحتوى", e)
            SourceType.M3U // Default fallback
        }
    }
    
    /**
     * اكتشاف portal endpoint الصحيح للـ Stalker - محسن
     */
    suspend fun discoverStalkerEndpoint(host: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "اكتشاف Stalker endpoint المحسن: $host")
            val cleanHost = cleanHostUrl(host)
            
            // استخدام قائمة endpoints المحسنة
            for (endpoint in ADVANCED_PORTAL_ENDPOINTS) {
                try {
                    val testUrl = "$cleanHost$endpoint"
                    Log.d(TAG, "اختبار endpoint محسن: $testUrl")
                    
                    val request = Request.Builder()
                        .url(testUrl)
                        .addHeader("User-Agent", ADVANCED_USER_AGENTS["MAG254"]!!)
                        .addHeader("X-User-Agent", "Model: MAG254; Link: Ethernet")
                        .build()
                    
                    val response = withTimeoutOrNull(3000) {
                        httpClient.newCall(request).execute()
                    }
                    
                    response?.use {
                        Log.d(TAG, "استجابة $endpoint: ${it.code}")
                        if (it.code in listOf(200, 401, 512)) {
                            Log.d(TAG, "تم العثور على endpoint صالح: $endpoint")
                            return@withContext endpoint
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.d(TAG, "فشل اختبار $endpoint: ${e.message}")
                    continue
                }
            }
            
            // إذا لم يتم العثور على endpoint، أعد الافتراضي
            Log.d(TAG, "لم يتم العثور على endpoint صالح، استخدام الافتراضي")
            return@withContext "/stalker_portal/server/load.php"
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في اكتشاف endpoint", e)
            return@withContext "/stalker_portal/server/load.php"
        }
    }
    
    /**
     * الحصول على معلومات الخادم الجغرافية - محسن
     */
    private suspend fun getEnhancedServerInfo(url: String): ServerInfo? {
        return try {
            Log.d(TAG, "الحصول على معلومات الخادم المحسنة: $url")
            val host = URL(url).host
            val protocol = URL(url).protocol
            val port = URL(url).port.takeIf { it != -1 }
            
            // محاولة الحصول على معلومات جغرافية
            val geoInfo = getServerGeoInfo(host)
            
            ServerInfo(
                host = host,
                protocol = protocol,
                port = port,
                lastPing = System.currentTimeMillis(),
                isOnline = true,
                country = geoInfo?.get("country"),
                countryCode = geoInfo?.get("countryCode"),
                city = geoInfo?.get("city"),
                isp = geoInfo?.get("isp"),
                timezone = geoInfo?.get("timezone")
            )
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في الحصول على معلومات الخادم", e)
            null
        }
    }
    
    /**
     * الحصول على معلومات جغرافية للخادم
     */
    private suspend fun getServerGeoInfo(host: String): Map<String, String>? {
        return try {
            val request = Request.Builder()
                .url("https://ipapi.co/$host/json/")
                .build()
            
            val response = withTimeoutOrNull(5000) {
                httpClient.newCall(request).execute()
            }
            
            response?.use {
                if (it.isSuccessful) {
                    val json = JSONObject(it.body?.string() ?: "")
                    
                    val country = json.optString("country_name", "Unknown")
                    val countryCode = json.optString("country_code", "")
                    val city = json.optString("city", "Unknown")
                    val isp = json.optString("org", "Unknown")
                    val timezone = json.optString("timezone", "UTC")
                    
                    mapOf(
                        "country" to country,
                        "countryCode" to countryCode,
                        "city" to city,
                        "isp" to isp,
                        "timezone" to TIMEZONE_COUNTRIES[timezone] ?: timezone
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في الحصول على معلومات جغرافية", e)
            null
        }
    }
    
    /**
     * فحص محسن لـ Stalker Portal
     */
    private suspend fun validateStalkerSourceAdvanced(
        url: String,
        macAddress: String,
        portalPath: String?,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            Log.d(TAG, "بدء التحقق المحسن من Stalker")
            
            // اكتشاف أفضل portal endpoint
            val detectedEndpoint = discoverStalkerEndpoint(url)
            Log.d(TAG, "تم اكتشاف endpoint: $detectedEndpoint")
            
            // اختبار الاتصال
            val connectionTest = testStalkerConnection(url, macAddress, detectedEndpoint)
            
            if (connectionTest.isSuccessful) {
                Log.d(TAG, "نجح اختبار Stalker connection")
                
                return SourceValidationResult(
                    isValid = true,
                    sourceType = SourceType.STALKER,
                    issues = issues,
                    warnings = warnings,
                    serverInfo = serverInfo,
                    detectedPortalPath = detectedEndpoint
                )
            } else {
                Log.e(TAG, "فشل اختبار Stalker connection: ${connectionTest.errorMessage}")
                issues.add("فشل في الاتصال: ${connectionTest.errorMessage}")
                
                return SourceValidationResult(
                    isValid = false,
                    sourceType = SourceType.STALKER,
                    issues = issues,
                    warnings = warnings,
                    serverInfo = serverInfo
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في التحقق من Stalker", e)
            issues.add("خطأ في التحقق: ${e.localizedMessage}")
            return SourceValidationResult(false, SourceType.STALKER, issues)
        }
    }
    
    /**
     * اختبار اتصال Stalker Portal
     */
    private suspend fun testStalkerConnection(
        url: String,
        macAddress: String,
        portalPath: String?
    ): ConnectionTestResult {
        return try {
            val cleanHost = cleanHostUrl(url)
            val testUrl = "$cleanHost${portalPath ?: "/stalker_portal/server/load.php"}"
            
            val request = Request.Builder()
                .url("$testUrl?type=stb&action=handshake&token=&JsHttpRequest=1-xml")
                .addHeader("User-Agent", ADVANCED_USER_AGENTS["MAG254"]!!)
                .addHeader("X-User-Agent", "Model: MAG254; Link: Ethernet")
                .addHeader("Cookie", "mac=${macAddress.replace(":", "%3A")}; stb_lang=en; timezone=Europe%2FParis;")
                .build()
            
            val response = withTimeoutOrNull(10000) {
                httpClient.newCall(request).execute()
            }
            
            response?.use {
                when (it.code) {
                    200 -> {
                        val body = it.body?.string() ?: ""
                        if (body.contains("token")) {
                            ConnectionTestResult(true, "اتصال ناجح")
                        } else {
                            ConnectionTestResult(false, "لا يحتوي على token صالح")
                        }
                    }
                    401 -> ConnectionTestResult(false, "MAC غير مصرح")
                    512 -> ConnectionTestResult(false, "مشكلة في الخادم")
                    else -> ConnectionTestResult(false, "كود خطأ: ${it.code}")
                }
            } ?: ConnectionTestResult(false, "لا توجد استجابة من الخادم")
            
        } catch (e: Exception) {
            ConnectionTestResult(false, "خطأ في الاتصال: ${e.message}")
        }
    }
    
    // Helper methods for URL detection
    private fun isM3UUrl(url: String): Boolean {
        return url.endsWith(".m3u", ignoreCase = true) || 
               url.endsWith(".m3u8", ignoreCase = true) || 
               url.contains("get.php", ignoreCase = true) ||
               url.contains("type=m3u", ignoreCase = true)
    }
    
    private fun isXtreamUrl(url: String): Boolean {
        return url.contains("player_api.php", ignoreCase = true) || 
               url.contains("xmltv.php", ignoreCase = true) ||
               url.contains("action=get_live_categories", ignoreCase = true)
    }
    
    private fun isStalkerUrl(url: String): Boolean {
        return ADVANCED_PORTAL_ENDPOINTS.any { endpoint ->
            url.contains(endpoint, ignoreCase = true)
        }
    }
    
    private fun isMacPortalUrl(url: String): Boolean {
        return url.contains("portal.php", ignoreCase = true) && 
               !url.contains("stalker_portal", ignoreCase = true)
    }
    
    // Data classes for results
    data class ConnectionTestResult(
        val isSuccessful: Boolean,
        val errorMessage: String? = null
    )
    
    /**
     * توليد عنوان MAC مناسب
     */
    fun generateMACAddress(prefix: String? = null): String {
        return stalkerService.generateMACAddress(prefix)
    }
    
    // Temporary simplified implementations for other methods
    private suspend fun validateXtreamSourceAdvanced(
        url: String,
        username: String,
        password: String,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        Log.d(TAG, "بدء التحقق المتقدم من Xtream Codes")
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            val result = xtreamService.validateSource(url, username, password)
            
            if (result.isValid) {
                Log.d(TAG, "تم التحقق من Xtream بنجاح عبر ${result.endpoint ?: "M3U Link"}")
                
                val accountInfo = AccountInfo(
                    username = username,
                    status = result.status,
                    expiryDate = result.expiryDate,
                    isActive = result.status?.equals("Active", ignoreCase = true) ?: true,
                    maxConnections = result.maxConnections,
                    activeConnections = result.activeConnections
                )
                
                // إضافة تحذير إذا كان الحساب سينتهي قريباً
                result.expiryDate?.let {
                    try {
                        val date = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).parse(it)
                        if (date != null) {
                            val daysLeft = TimeUnit.MILLISECONDS.toDays(date.time - System.currentTimeMillis())
                            if (daysLeft in 0..7) {
                                warnings.add("⚠️ الحساب سينتهي خلال $daysLeft أيام")
                            }
                        }
                    } catch (e: Exception) { /* تجاهل أخطاء التنسيق */ }
                }
                
                return SourceValidationResult(
                    isValid = true,
                    sourceType = SourceType.XTREAM,
                    issues = issues,
                    warnings = warnings,
                    serverInfo = serverInfo?.copy(host = result.realUrl ?: serverInfo.host, timezone = result.timezone),
                    accountInfo = accountInfo,
                    detectedUserAgent = "Xtream Codes Player"
                )
            } else {
                Log.e(TAG, "فشل التحقق من Xtream")
                issues.add("فشل التحقق من Xtream: لا توجد صيغة أو endpoint صالح")
                return SourceValidationResult(false, SourceType.XTREAM, issues, serverInfo = serverInfo)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في التحقق من Xtream", e)
            issues.add("خطأ في التحقق: ${e.localizedMessage}")
            return SourceValidationResult(false, SourceType.XTREAM, issues, serverInfo = serverInfo)
        }
    }
    
    private suspend fun validateM3USourceAdvanced(
        url: String,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            Log.d(TAG, "بدء تحليل M3U المحسن: $url")
            
            val testResult = testUrlAccess(url)
            if (!testResult) {
                Log.e(TAG, "لا يمكن الوصول إلى الرابط")
                issues.add("لا يمكن الوصول إلى الرابط")
                return SourceValidationResult(false, SourceType.M3U, issues)
            }
            
            val parseResult = m3uParser.parseFromUrl(url, 0L)
            
            when (parseResult) {
                is M3UParser.ParseResult.Success -> {
                    Log.d(TAG, "نجح تحليل M3U المحسن: ${parseResult.statistics.totalChannels} قناة")
                    val stats = parseResult.statistics
                    
                    if (stats.totalChannels == 0) {
                        issues.add("لا يحتوي الملف على أي قنوات صالحة")
                        return SourceValidationResult(false, SourceType.M3U, issues)
                    }
                    
                    if (stats.totalChannels < 10) {
                        warnings.add("عدد قليل من القنوات (${stats.totalChannels})")
                    }
                    
                    val statistics = SourceStatistics(
                        sourceId = 0L,
                        totalChannels = stats.totalChannels,
                        liveChannels = stats.liveChannels,
                        vodChannels = stats.vodChannels,
                        seriesChannels = stats.seriesChannels,
                        radioChannels = stats.radioChannels,
                        channelsWithEpg = stats.channelsWithEpg,
                        channelsWithLogo = stats.channelsWithLogo,
                        totalCategories = stats.categories.size,
                        lastUpdated = System.currentTimeMillis()
                    )
                    
                    return SourceValidationResult(
                        isValid = true,
                        sourceType = SourceType.M3U,
                        issues = issues,
                        warnings = warnings,
                        statistics = statistics,
                        serverInfo = serverInfo
                    )
                }
                
                is M3UParser.ParseResult.Error -> {
                    Log.e(TAG, "فشل تحليل M3U: ${parseResult.message}")
                    issues.add("فشل في تحليل M3U: ${parseResult.message}")
                    return SourceValidationResult(false, SourceType.M3U, issues)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في التحقق من M3U المحسن", e)
            issues.add("خطأ في التحقق من M3U: ${e.localizedMessage ?: e.message}")
            return SourceValidationResult(false, SourceType.M3U, issues)
        }
    }
    
    private suspend fun validateMacPortalSourceAdvanced(
        url: String,
        macAddress: String,
        portalPath: String?,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        Log.d(TAG, "تحقق محسن من MAC Portal")
        return validateStalkerSourceAdvanced(url, macAddress, portalPath ?: "/portal.php", serverInfo)
    }
    
    private suspend fun testUrlAccess(url: String): Boolean {
        return try {
            Log.d(TAG, "اختبار الوصول إلى: $url")
            val request = Request.Builder()
                .url(url)
                .head()
                .addHeader("User-Agent", "IPTV Player/1.0")
                .build()
            
            val response = withTimeoutOrNull(10000) {
                httpClient.newCall(request).execute()
            }
            
            val isSuccessful = response?.use { it.isSuccessful } ?: false
            Log.d(TAG, "نتيجة اختبار الوصول: $isSuccessful")
            isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في اختبار الوصول", e)
            false
        }
    }
    
    // Helper methods
    private fun isValidUrl(url: String): Boolean {
        return try {
            URL(url)
            url.startsWith("http://", ignoreCase = true) || 
            url.startsWith("https://", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun cleanHostUrl(url: String): String {
        return try {
            val uri = URL(url)
            "${uri.protocol}://${uri.authority}"
        } catch (e: Exception) {
            url
        }
    }
}