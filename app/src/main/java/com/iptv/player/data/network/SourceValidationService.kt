package com.iptv.player.data.network

import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import android.util.Log
import java.net.InetAddress
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceValidationService @Inject constructor(
    private val stalkerService: StalkerService,
    private val xtreamService: XtreamService,
    private val m3uParser: M3UParser
) {
    
    companion object {
        private const val TAG = "SourceValidationService"
    }
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    /**
     * التحقق الشامل من صحة المصدر - مع تحسينات
     */
    suspend fun validateSource(
        sourceType: SourceType,
        url: String,
        username: String? = null,
        password: String? = null,
        macAddress: String? = null,
        portalPath: String? = null
    ): SourceValidationResult = withContext(Dispatchers.IO) {
        
        Log.d(TAG, "بدء التحقق من المصدر: $sourceType - $url")
        
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
            
            Log.d(TAG, "الرابط صحيح، جاري الحصول على معلومات الخادم...")
            
            // الحصول على معلومات الخادم
            val serverInfo = getServerInfo(url)
            Log.d(TAG, "معلومات الخادم: $serverInfo")
            
            // التحقق حسب نوع المصدر
            when (sourceType) {
                SourceType.STALKER -> {
                    Log.d(TAG, "التحقق من Stalker Portal...")
                    if (macAddress.isNullOrEmpty()) {
                        issues.add("عنوان MAC مطلوب للـ Stalker Portal")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    if (!stalkerService.isValidMAC(macAddress)) {
                        issues.add("عنوان MAC غير صحيح")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    return@withContext validateStalkerSource(url, macAddress, portalPath, serverInfo)
                }
                
                SourceType.XTREAM -> {
                    Log.d(TAG, "التحقق من Xtream Codes...")
                    if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
                        issues.add("اسم المستخدم وكلمة المرور مطلوبان للـ Xtream Codes")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    return@withContext validateXtreamSource(url, username, password, serverInfo)
                }
                
                SourceType.M3U -> {
                    Log.d(TAG, "التحقق من M3U...")
                    return@withContext validateM3USource(url, serverInfo)
                }
                
                SourceType.MAC_PORTAL -> {
                    Log.d(TAG, "التحقق من MAC Portal...")
                    if (macAddress.isNullOrEmpty()) {
                        issues.add("عنوان MAC مطلوب للـ MAC Portal")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    return@withContext validateMacPortalSource(url, macAddress, portalPath, serverInfo)
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
     * اكتشاف نوع المصدر تلقائياً - مبسط
     */
    suspend fun detectSourceType(url: String): SourceType? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "اكتشاف نوع المصدر: $url")
            
            // فحص رابط M3U
            if (url.endsWith(".m3u", ignoreCase = true) || 
                url.endsWith(".m3u8", ignoreCase = true) || 
                url.contains("get.php", ignoreCase = true)) {
                Log.d(TAG, "تم اكتشاف نوع M3U")
                return@withContext SourceType.M3U
            }
            
            // فحص Xtream Codes
            if (url.contains("player_api.php", ignoreCase = true) || 
                url.contains("xmltv.php", ignoreCase = true)) {
                Log.d(TAG, "تم اكتشاف نوع Xtream")
                return@withContext SourceType.XTREAM
            }
            
            // فحص Stalker Portal
            for (endpoint in StalkerService.PORTAL_ENDPOINTS) {
                if (url.contains(endpoint, ignoreCase = true)) {
                    Log.d(TAG, "تم اكتشاف نوع Stalker")
                    return@withContext SourceType.STALKER
                }
            }
            
            // إذا لم يتم اكتشاف النوع، افترض M3U كافتراضي
            Log.d(TAG, "لم يتم اكتشاف نوع محدد، افتراض M3U")
            return@withContext SourceType.M3U
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في اكتشاف نوع المصدر", e)
            return@withContext null
        }
    }
    
    /**
     * اكتشاف portal endpoint الصحيح للـ Stalker - مبسط
     */
    suspend fun discoverStalkerEndpoint(host: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "اكتشاف Stalker endpoint: $host")
            val cleanHost = cleanHostUrl(host)
            
            // جرب أشهر endpoints أولاً
            val commonEndpoints = listOf(
                "/stalker_portal/server/load.php",
                "/portal.php",
                "/server/load.php",
                "/c/portal.php"
            )
            
            for (endpoint in commonEndpoints) {
                try {
                    val testUrl = "$cleanHost$endpoint"
                    Log.d(TAG, "اختبار endpoint: $testUrl")
                    
                    val request = Request.Builder()
                        .url(testUrl)
                        .addHeader("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Safari/533.3")
                        .build()
                    
                    val response = withTimeoutOrNull(5000) {
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
     * توليد عنوان MAC مناسب
     */
    fun generateMACAddress(prefix: String? = null): String {
        return stalkerService.generateMACAddress(prefix)
    }
    
    /**
     * التحقق من ملف M3U - مبسط
     */
    private suspend fun validateM3USource(
        url: String,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            Log.d(TAG, "بدء تحليل M3U: $url")
            
            // تحقق مبسط من الوصول للرابط
            val testResult = testUrlAccess(url)
            if (!testResult) {
                Log.e(TAG, "لا يمكن الوصول إلى الرابط")
                issues.add("لا يمكن الوصول إلى الرابط")
                return SourceValidationResult(false, SourceType.M3U, issues)
            }
            
            // محاولة تحليل M3U
            val parseResult = m3uParser.parseFromUrl(url, 0L)
            
            when (parseResult) {
                is M3UParser.ParseResult.Success -> {
                    Log.d(TAG, "نجح تحليل M3U: ${parseResult.statistics.totalChannels} قناة")
                    val stats = parseResult.statistics
                    
                    if (stats.totalChannels == 0) {
                        issues.add("لا يحتوي الملف على أي قنوات صالحة")
                        return SourceValidationResult(false, SourceType.M3U, issues)
                    }
                    
                    if (stats.totalChannels < 10) {
                        warnings.add("عدد قليل من القنوات (${stats.totalChannels})")
                    }
                    
                    if (!stats.hasValidHeader) {
                        warnings.add("لا يحتوي على header صحيح")
                    }
                    
                    if (stats.channelsWithEpg == 0) {
                        warnings.add("لا توجد معلومات EPG")
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
                    
                    Log.d(TAG, "تم التحقق من M3U بنجاح")
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
            Log.e(TAG, "خطأ في التحقق من M3U", e)
            issues.add("خطأ في التحقق من M3U: ${e.localizedMessage ?: e.message}")
            return SourceValidationResult(false, SourceType.M3U, issues)
        }
    }
    
    /**
     * اختبار بسيط للوصول إلى الرابط
     */
    private suspend fun testUrlAccess(url: String): Boolean {
        return try {
            Log.d(TAG, "اختبار الوصول إلى: $url")
            val request = Request.Builder()
                .url(url)
                .head() // استخدام HEAD request للسرعة
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
    
    // باقي الدوال مبسطة مؤقتاً للاختبار...
    
    private suspend fun validateStalkerSource(
        url: String,
        macAddress: String,
        portalPath: String?,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        Log.d(TAG, "تحقق مبسط من Stalker")
        // تحقق مبسط مؤقتاً
        return SourceValidationResult(
            isValid = true,
            sourceType = SourceType.STALKER,
            issues = emptyList(),
            warnings = listOf("تم قبول المصدر بدون تحقق كامل (وضع التطوير)"),
            serverInfo = serverInfo
        )
    }
    
    private suspend fun validateXtreamSource(
        url: String,
        username: String,
        password: String,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        Log.d(TAG, "تحقق مبسط من Xtream")
        // تحقق مبسط مؤقتاً
        return SourceValidationResult(
            isValid = true,
            sourceType = SourceType.XTREAM,
            issues = emptyList(),
            warnings = listOf("تم قبول المصدر بدون تحقق كامل (وضع التطوير)"),
            serverInfo = serverInfo
        )
    }
    
    private suspend fun validateMacPortalSource(
        url: String,
        macAddress: String,
        portalPath: String?,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        Log.d(TAG, "تحقق مبسط من MAC Portal")
        return validateStalkerSource(url, macAddress, portalPath ?: "/portal.php", serverInfo)
    }
    
    /**
     * الحصول على معلومات الخادم الجغرافية - مبسط
     */
    private suspend fun getServerInfo(url: String): ServerInfo? {
        return try {
            Log.d(TAG, "الحصول على معلومات الخادم: $url")
            val host = URL(url).host
            
            ServerInfo(
                host = host,
                protocol = URL(url).protocol,
                port = URL(url).port.takeIf { it != -1 },
                lastPing = System.currentTimeMillis(),
                isOnline = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في الحصول على معلومات الخادم", e)
            null
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