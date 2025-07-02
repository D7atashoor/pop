package com.iptv.player.data.network

import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    /**
     * التحقق الشامل من صحة المصدر
     */
    suspend fun validateSource(
        sourceType: SourceType,
        url: String,
        username: String? = null,
        password: String? = null,
        macAddress: String? = null,
        portalPath: String? = null
    ): SourceValidationResult = withContext(Dispatchers.IO) {
        
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            // التحقق من صحة الرابط
            if (!isValidUrl(url)) {
                issues.add("رابط غير صحيح")
                return@withContext SourceValidationResult(
                    isValid = false,
                    sourceType = null,
                    issues = issues
                )
            }
            
            // الحصول على معلومات الخادم
            val serverInfo = getServerInfo(url)
            
            // التحقق حسب نوع المصدر
            when (sourceType) {
                SourceType.STALKER -> {
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
                    if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
                        issues.add("اسم المستخدم وكلمة المرور مطلوبان للـ Xtream Codes")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    return@withContext validateXtreamSource(url, username, password, serverInfo)
                }
                
                SourceType.M3U -> {
                    return@withContext validateM3USource(url, serverInfo)
                }
                
                SourceType.MAC_PORTAL -> {
                    if (macAddress.isNullOrEmpty()) {
                        issues.add("عنوان MAC مطلوب للـ MAC Portal")
                        return@withContext SourceValidationResult(false, sourceType, issues)
                    }
                    
                    return@withContext validateMacPortalSource(url, macAddress, portalPath, serverInfo)
                }
            }
            
        } catch (e: Exception) {
            issues.add("خطأ في التحقق من المصدر: ${e.message}")
            return@withContext SourceValidationResult(
                isValid = false,
                sourceType = sourceType,
                issues = issues
            )
        }
    }
    
    /**
     * اكتشاف نوع المصدر تلقائياً
     */
    suspend fun detectSourceType(url: String): SourceType? = withContext(Dispatchers.IO) {
        try {
            // فحص رابط M3U
            if (url.endsWith(".m3u") || url.endsWith(".m3u8") || url.contains("get.php")) {
                return@withContext SourceType.M3U
            }
            
            // فحص Xtream Codes
            if (url.contains("player_api.php") || url.contains("xmltv.php")) {
                return@withContext SourceType.XTREAM
            }
            
            // فحص Stalker Portal
            for (endpoint in StalkerService.PORTAL_ENDPOINTS) {
                if (url.contains(endpoint)) {
                    return@withContext SourceType.STALKER
                }
            }
            
            // اختبار الاستجابة للتحديد
            val testResults = listOf(
                async { testStalkerEndpoint(url) },
                async { testXtreamEndpoint(url) },
                async { testM3UEndpoint(url) }
            )
            
            val results = testResults.map { it.await() }
            
            return@withContext when {
                results[0] -> SourceType.STALKER
                results[1] -> SourceType.XTREAM
                results[2] -> SourceType.M3U
                else -> null
            }
            
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * اكتشاف portal endpoint الصحيح للـ Stalker
     */
    suspend fun discoverStalkerEndpoint(host: String): String? = withContext(Dispatchers.IO) {
        val cleanHost = cleanHostUrl(host)
        
        for (endpoint in StalkerService.PORTAL_ENDPOINTS) {
            try {
                val testUrl = "$cleanHost$endpoint"
                val request = Request.Builder()
                    .url(testUrl)
                    .addHeader("User-Agent", "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 4 rev: 1812 Safari/533.3")
                    .build()
                
                val response = withTimeoutOrNull(5000) {
                    httpClient.newCall(request).execute()
                }
                
                response?.use {
                    if (it.code in listOf(200, 401, 512)) {
                        return@withContext endpoint
                    }
                }
                
            } catch (e: Exception) {
                continue
            }
        }
        
        return@withContext null
    }
    
    /**
     * توليد عنوان MAC مناسب
     */
    fun generateMACAddress(prefix: String? = null): String {
        return stalkerService.generateMACAddress(prefix)
    }
    
    /**
     * التحقق من خادم Stalker
     */
    private suspend fun validateStalkerSource(
        url: String,
        macAddress: String,
        portalPath: String?,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            val cleanHost = cleanHostUrl(url)
            val discoveredPath = portalPath ?: discoverStalkerEndpoint(cleanHost)
            
            if (discoveredPath == null) {
                issues.add("لم يتم العثور على portal endpoint صحيح")
                return SourceValidationResult(false, SourceType.STALKER, issues)
            }
            
            // محاولة الاتصال
            val isConnected = stalkerService.initialize(cleanHost, macAddress)
            
            if (!isConnected) {
                issues.add("فشل في الاتصال بـ Stalker Portal")
                warnings.add("تحقق من عنوان MAC أو رابط الخادم")
                return SourceValidationResult(false, SourceType.STALKER, issues, warnings)
            }
            
            // الحصول على معلومات الحساب
            val accountInfo = try {
                val profile = stalkerService.getProfile()
                val accInfo = stalkerService.getAccountInfo()
                AccountInfo(
                    username = profile?.login,
                    status = if (profile?.status == 1) "Active" else "Inactive",
                    expiryDate = profile?.exp_date,
                    isActive = profile?.status == 1,
                    maxConnections = null,
                    activeConnections = null
                )
            } catch (e: Exception) {
                warnings.add("لا يمكن الحصول على معلومات الحساب")
                null
            }
            
            // إحصائيات سريعة
            val statistics = try {
                val channels = stalkerService.getChannels(0L)
                SourceStatistics(
                    sourceId = 0L,
                    totalChannels = channels.size,
                    liveChannels = channels.size,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                warnings.add("لا يمكن الحصول على إحصائيات القنوات")
                null
            }
            
            return SourceValidationResult(
                isValid = true,
                sourceType = SourceType.STALKER,
                issues = issues,
                warnings = warnings,
                statistics = statistics,
                serverInfo = serverInfo,
                accountInfo = accountInfo
            )
            
        } catch (e: Exception) {
            issues.add("خطأ في التحقق من Stalker Portal: ${e.message}")
            return SourceValidationResult(false, SourceType.STALKER, issues)
        }
    }
    
    /**
     * التحقق من خادم Xtream
     */
    private suspend fun validateXtreamSource(
        url: String,
        username: String,
        password: String,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            val cleanHost = cleanHostUrl(url)
            val authResult = xtreamService.authenticate(cleanHost, username, password)
            
            if (authResult == null) {
                issues.add("فشل في المصادقة")
                warnings.add("تحقق من اسم المستخدم وكلمة المرور")
                return SourceValidationResult(false, SourceType.XTREAM, issues, warnings)
            }
            
            // فحص انتهاء الحساب
            if (xtreamService.isAccountExpired(authResult.user_info)) {
                warnings.add("الحساب منتهي الصلاحية")
            }
            
            // معلومات الحساب
            val accountInfo = AccountInfo(
                username = authResult.user_info.username,
                status = authResult.user_info.status,
                expiryDate = xtreamService.formatExpiryDate(authResult.user_info),
                isActive = authResult.user_info.auth == 1,
                isTrial = authResult.user_info.is_trial == "1",
                maxConnections = authResult.user_info.max_connections.toIntOrNull(),
                activeConnections = authResult.user_info.active_cons.toIntOrNull(),
                createdAt = authResult.user_info.created_at,
                allowedOutputFormats = authResult.user_info.allowed_output_formats
            )
            
            // إحصائيات سريعة
            val statistics = try {
                val liveChannels = xtreamService.getChannels(cleanHost, username, password, 0L)
                val movies = xtreamService.getMovies(cleanHost, username, password, 0L)
                val series = xtreamService.getSeries(cleanHost, username, password, 0L)
                
                SourceStatistics(
                    sourceId = 0L,
                    totalChannels = liveChannels.size + movies.size + series.size,
                    liveChannels = liveChannels.size,
                    vodChannels = movies.size,
                    seriesChannels = series.size,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                warnings.add("لا يمكن الحصول على إحصائيات المحتوى")
                null
            }
            
            return SourceValidationResult(
                isValid = true,
                sourceType = SourceType.XTREAM,
                issues = issues,
                warnings = warnings,
                statistics = statistics,
                serverInfo = serverInfo,
                accountInfo = accountInfo
            )
            
        } catch (e: Exception) {
            issues.add("خطأ في التحقق من Xtream Codes: ${e.message}")
            return SourceValidationResult(false, SourceType.XTREAM, issues)
        }
    }
    
    /**
     * التحقق من ملف M3U
     */
    private suspend fun validateM3USource(
        url: String,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            val parseResult = m3uParser.parseFromUrl(url, 0L)
            
            when (parseResult) {
                is M3UParser.ParseResult.Success -> {
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
                    issues.add("فشل في تحليل M3U: ${parseResult.message}")
                    return SourceValidationResult(false, SourceType.M3U, issues)
                }
            }
            
        } catch (e: Exception) {
            issues.add("خطأ في التحقق من M3U: ${e.message}")
            return SourceValidationResult(false, SourceType.M3U, issues)
        }
    }
    
    /**
     * التحقق من MAC Portal
     */
    private suspend fun validateMacPortalSource(
        url: String,
        macAddress: String,
        portalPath: String?,
        serverInfo: ServerInfo?
    ): SourceValidationResult {
        // منطق مشابه للـ Stalker لكن مع تعديلات للـ MAC Portal
        return validateStalkerSource(url, macAddress, portalPath ?: "/portal.php", serverInfo)
    }
    
    /**
     * الحصول على معلومات الخادم الجغرافية
     */
    private suspend fun getServerInfo(url: String): ServerInfo? {
        return try {
            val host = URL(url).host
            val address = withTimeoutOrNull(5000) {
                InetAddress.getByName(host)
            }
            
            address?.let {
                ServerInfo(
                    host = host,
                    protocol = URL(url).protocol,
                    port = URL(url).port.takeIf { it != -1 },
                    lastPing = System.currentTimeMillis(),
                    isOnline = true
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Helper methods
    private fun isValidUrl(url: String): Boolean {
        return try {
            URL(url)
            url.startsWith("http://") || url.startsWith("https://")
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
    
    private suspend fun testStalkerEndpoint(url: String): Boolean {
        return try {
            val cleanHost = cleanHostUrl(url)
            discoverStalkerEndpoint(cleanHost) != null
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun testXtreamEndpoint(url: String): Boolean {
        return try {
            val testUrl = "${cleanHostUrl(url)}/player_api.php"
            val request = Request.Builder().url(testUrl).build()
            
            val response = withTimeoutOrNull(5000) {
                httpClient.newCall(request).execute()
            }
            
            response?.use { it.code in listOf(200, 400) } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun testM3UEndpoint(url: String): Boolean {
        return try {
            val request = Request.Builder().url(url).build()
            
            val response = withTimeoutOrNull(5000) {
                httpClient.newCall(request).execute()
            }
            
            response?.use { 
                it.isSuccessful && 
                (it.body?.string()?.contains("#EXTM3U") == true ||
                 it.header("Content-Type")?.contains("audio/x-mpegurl") == true ||
                 it.header("Content-Type")?.contains("application/vnd.apple.mpegurl") == true)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}