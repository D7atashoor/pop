package com.iptv.player.data.network

import com.iptv.player.data.model.Channel
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

interface StalkerApiService {
    
    @FormUrlEncoded
    @POST("server/load.php")
    suspend fun handshake(
        @Field("type") type: String = "stb",
        @Field("action") action: String = "handshake",
        @Field("token") token: String = "",
        @Field("JsHttpRequest") jsHttpRequest: String = "1-xml"
    ): Response<StalkerHandshakeResponse>
    
    @FormUrlEncoded
    @POST("server/load.php")
    suspend fun getProfile(
        @Field("type") type: String = "stb",
        @Field("action") action: String = "get_profile",
        @Field("JsHttpRequest") jsHttpRequest: String = "1-xml",
        @Field("authorization") authorization: String
    ): Response<StalkerProfileResponse>
    
    @FormUrlEncoded
    @POST("server/load.php")
    suspend fun getAccountInfo(
        @Field("type") type: String = "account_info",
        @Field("action") action: String = "get_main_info",
        @Field("JsHttpRequest") jsHttpRequest: String = "1-xml",
        @Field("authorization") authorization: String
    ): Response<StalkerAccountInfoResponse>
    
    @FormUrlEncoded
    @POST("server/load.php")
    suspend fun getGenres(
        @Field("type") type: String = "itv",
        @Field("action") action: String = "get_genres",
        @Field("JsHttpRequest") jsHttpRequest: String = "1-xml",
        @Field("authorization") authorization: String
    ): Response<StalkerGenresResponse>
    
    @FormUrlEncoded
    @POST("server/load.php")
    suspend fun getOrderedList(
        @Field("type") type: String = "itv",
        @Field("action") action: String = "get_ordered_list",
        @Field("genre") genre: String = "*",
        @Field("force_ch_link_check") forceChannelLinkCheck: String = "",
        @Field("fav") fav: String = "0",
        @Field("sortby") sortBy: String = "number",
        @Field("p") page: Int = 1,
        @Field("JsHttpRequest") jsHttpRequest: String = "1-xml",
        @Field("authorization") authorization: String
    ): Response<StalkerChannelsResponse>
    
    @FormUrlEncoded
    @POST("server/load.php")
    suspend fun getVODCategories(
        @Field("type") type: String = "vod",
        @Field("action") action: String = "get_categories",
        @Field("JsHttpRequest") jsHttpRequest: String = "1-xml",
        @Field("authorization") authorization: String
    ): Response<StalkerVODCategoriesResponse>
    
    @FormUrlEncoded
    @POST("server/load.php")
    suspend fun getSeriesCategories(
        @Field("type") type: String = "series",
        @Field("action") action: String = "get_categories",
        @Field("JsHttpRequest") jsHttpRequest: String = "1-xml",
        @Field("authorization") authorization: String
    ): Response<StalkerSeriesCategoriesResponse>
    
    @FormUrlEncoded
    @POST("server/load.php")
    suspend fun createChannelLink(
        @Field("type") type: String = "itv",
        @Field("action") action: String = "create_link",
        @Field("cmd") cmd: String,
        @Field("JsHttpRequest") jsHttpRequest: String = "1-xml",
        @Field("authorization") authorization: String
    ): Response<StalkerCreateLinkResponse>
}

data class StalkerHandshakeResponse(
    val js: StalkerHandshake?
)

data class StalkerHandshake(
    val token: String,
    val random: String
)

data class StalkerProfileResponse(
    val js: StalkerProfile?
)

data class StalkerProfile(
    val id: String,
    val login: String,
    val fname: String,
    val lname: String,
    val account_number: String,
    val tariff_plan: String,
    val status: Int,
    val exp_date: String? = null,
    val phone: String? = null
)

data class StalkerAccountInfoResponse(
    val js: StalkerAccountInfo?
)

data class StalkerAccountInfo(
    val mac: String,
    val phone: String,
    val ls: String,
    val version: String,
    val lang: String,
    val locale: String,
    val city_id: String,
    val hd: String,
    val main_notify: String,
    val fav_itv_on: String,
    val now_playing_start: String,
    val now_playing_type: String,
    val playback_buffer_bytes: String,
    val playback_buffer_size: String,
    val plasma_saving: String,
    val sleep_on_off: String
)

data class StalkerGenresResponse(
    val js: List<StalkerGenre>?
)

data class StalkerGenre(
    val id: String,
    val title: String,
    val alias: String,
    val censored: String
)

data class StalkerChannelsResponse(
    val js: StalkerChannelData?
)

data class StalkerChannelData(
    val data: List<StalkerChannel>?,
    val total_items: String,
    val max_page_items: String,
    val selected_item: String,
    val cur_page: String,
    val total_pages: String
)

data class StalkerChannel(
    val id: String,
    val name: String,
    val number: String,
    val tv_genre_id: String,
    val base_ch: String,
    val cmd: String,
    val cost: String,
    val count: String,
    val status: String,
    val hd: String,
    val xmltv_id: String,
    val service_id: String,
    val bonus_ch: String,
    val volume_correction: String,
    val logo: String,
    val enable_tv_archive: String,
    val tv_archive_duration: String,
    val lock: String,
    val fav: String,
    val censored: String,
    val base_color: String,
    val epg_start: String,
    val epg_end: String,
    val open: String,
    val mc_cmd: String
)

data class StalkerVODCategoriesResponse(
    val js: StalkerVODCategoryData?
)

data class StalkerVODCategoryData(
    val data: List<StalkerCategory>?
)

data class StalkerSeriesCategoriesResponse(
    val js: StalkerSeriesCategoryData?
)

data class StalkerSeriesCategoryData(
    val data: List<StalkerCategory>?
)

data class StalkerCategory(
    val id: String,
    val title: String,
    val alias: String,
    val censored: String
)

data class StalkerCreateLinkResponse(
    val js: StalkerCreateLinkData?
)

data class StalkerCreateLinkData(
    val cmd: String,
    val id: String
)

@Singleton
class StalkerService @Inject constructor(
    private val apiService: StalkerApiService
) {
    
    companion object {
        // قائمة شاملة بأنواع البوابات المدعومة - مستخرجة من التحليل
        val PORTAL_ENDPOINTS = listOf(
            "/portal.php",
            "/server/load.php", 
            "/stalker_portal/server/load.php",
            "/stalker_u.php",
            "/BoSSxxxx/portal.php",
            "/c/portal.php",
            "/c/server/load.php",
            "/magaccess/portal.php",
            "/portalcc.php",
            "/bs.mag.portal.php",
            "/magportal/portal.php",
            "/maglove/portal.php",
            "/tek/server/load.php",
            "/emu/server/load.php",
            "/emu2/server/load.php",
            "/xx//server/load.php",
            "/portalott.php",
            "/ghandi_portal/server/load.php",
            "/magLoad.php",
            "/ministra/portal.php",
            "/portalstb/portal.php",
            "/xx/portal.php",
            "/portalmega.php",
            "/portalmega/portal.php",
            "/rmxportal/portal.php",
            "/portalmega/portalmega.php",
            "/powerfull/portal.php",
            "/korisnici/server/load.php",
            "/nettvmag/portal.php",
            "/cmdforex/portal.php",
            "/k/portal.php",
            "/p/portal.php",
            "/cp/server/load.php",
            "/extraportal.php",
            "/Link_Ok/portal.php",
            "/delko/portal.php",
            "/delko/server/load.php",
            "/bStream/portal.php",
            "/bStream/server/load.php",
            "/blowportal/portal.php",
            "/client/portal.php",
            "/server/move.php"
        )
        
        // بادئات MAC شائعة لأجهزة MAG
        val MAC_PREFIXES = listOf(
            "00:1A:79:", "78:A3:52:", "10:27:BE:", "6C:0D:C4:", 
            "A0:BB:3E:", "D0:9F:D9:", "04:D6:AA:", "11:33:01:", 
            "00:1C:19:", "1A:00:6A:", "1A:00:FB:", "00:A1:79:",
            "00:1B:79:", "00:2A:79:", "D4:CF:F9:", "33:44:CF:"
        )
    }
    
    private var token: String? = null
    private var authorization: String? = null
    private var macAddress: String? = null
    
    /**
     * توليد عنوان MAC عشوائي لأجهزة MAG
     */
    fun generateMACAddress(prefix: String? = null): String {
        val selectedPrefix = prefix ?: MAC_PREFIXES.random()
        val randomBytes = ByteArray(3)
        Random.nextBytes(randomBytes)
        
        val suffix = randomBytes.joinToString(":") { "%02X".format(it) }
        return selectedPrefix + suffix
    }
    
    /**
     * التحقق من صحة عنوان MAC
     */
    fun isValidMAC(mac: String): Boolean {
        val macPattern = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
        return macPattern.matches(mac)
    }
    
    /**
     * توليد بيانات اعتماد الجهاز من عنوان MAC
     */
    fun generateDeviceCredentials(mac: String): DeviceCredentials {
        val macClean = mac.uppercase().replace(":", "")
        
        // Serial Number
        val snHash = MessageDigest.getInstance("MD5").digest(mac.toByteArray())
        val serialNumber = snHash.joinToString("") { "%02X".format(it) }.take(13)
        
        // Device ID
        val deviceIdHash = MessageDigest.getInstance("SHA-256").digest(mac.toByteArray())
        val deviceId = deviceIdHash.joinToString("") { "%02X".format(it) }.uppercase()
        
        // Device ID 2
        val deviceId2Hash = MessageDigest.getInstance("SHA-256").digest(serialNumber.toByteArray())
        val deviceId2 = deviceId2Hash.joinToString("") { "%02X".format(it) }.uppercase()
        
        // Signature
        val signatureInput = serialNumber + mac
        val signatureHash = MessageDigest.getInstance("SHA-256").digest(signatureInput.toByteArray())
        val signature = signatureHash.joinToString("") { "%02X".format(it) }.uppercase()
        
        return DeviceCredentials(
            mac = mac.uppercase(),
            serialNumber = serialNumber,
            deviceId = deviceId,
            deviceId2 = deviceId2,
            signature = signature,
            stbType = "MAG254"
        )
    }
    
    /**
     * اكتشاف أفضل portal endpoint للخادم
     */
    suspend fun discoverPortalEndpoint(host: String): String? {
        // يمكن إضافة منطق للتحقق من كل endpoint
        // في الوقت الحالي نعيد الافتراضي
        return "/stalker_portal/server/load.php"
    }
    
    /**
     * تهيئة الاتصال مع Stalker Portal
     */
    suspend fun initialize(portalUrl: String, macAddress: String): Boolean {
        try {
            this.macAddress = macAddress
            
            // التحقق من صحة MAC
            if (!isValidMAC(macAddress)) {
                return false
            }
            
            // إجراء handshake
            val handshakeResponse = apiService.handshake()
            if (handshakeResponse.isSuccessful) {
                token = handshakeResponse.body()?.js?.token
                
                if (token != null) {
                    // إنشاء authorization token محسن
                    authorization = generateEnhancedAuthorization(macAddress, token!!)
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    
    /**
     * الحصول على معلومات الحساب التفصيلية
     */
    suspend fun getAccountInfo(): StalkerAccountInfo? {
        if (authorization == null) return null
        
        try {
            val response = apiService.getAccountInfo(authorization = authorization!!)
            if (response.isSuccessful) {
                return response.body()?.js
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    /**
     * الحصول على معلومات الملف الشخصي
     */
    suspend fun getProfile(): StalkerProfile? {
        if (authorization == null) return null
        
        try {
            val response = apiService.getProfile(authorization = authorization!!)
            if (response.isSuccessful) {
                return response.body()?.js
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    /**
     * الحصول على القنوات مع تحسينات
     */
    suspend fun getChannels(sourceId: Long): List<Channel> {
        if (authorization == null) return emptyList()
        
        try {
            val response = apiService.getOrderedList(authorization = authorization!!)
            if (response.isSuccessful) {
                return response.body()?.js?.data?.map { stalkerChannel ->
                    Channel(
                        id = stalkerChannel.id,
                        name = cleanChannelName(stalkerChannel.name),
                        group = null, // سيتم ملؤها من الأنواع
                        logo = stalkerChannel.logo.takeIf { it.isNotEmpty() },
                        url = stalkerChannel.cmd,
                        epgId = stalkerChannel.xmltv_id.takeIf { it.isNotEmpty() },
                        sourceId = sourceId
                    )
                } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    /**
     * إنشاء رابط تشغيل للقناة
     */
    suspend fun createChannelStreamLink(channelId: String): String? {
        if (authorization == null) return null
        
        try {
            val cmd = "ffmpeg http://localhost/ch/${channelId}_"
            val response = apiService.createChannelLink(
                cmd = cmd,
                authorization = authorization!!
            )
            
            if (response.isSuccessful) {
                return response.body()?.js?.cmd
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    /**
     * الحصول على فئات VOD
     */
    suspend fun getVODCategories(): List<StalkerCategory> {
        if (authorization == null) return emptyList()
        
        try {
            val response = apiService.getVODCategories(authorization = authorization!!)
            if (response.isSuccessful) {
                return response.body()?.js?.data ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    /**
     * الحصول على فئات المسلسلات
     */
    suspend fun getSeriesCategories(): List<StalkerCategory> {
        if (authorization == null) return emptyList()
        
        try {
            val response = apiService.getSeriesCategories(authorization = authorization!!)
            if (response.isSuccessful) {
                return response.body()?.js?.data ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    /**
     * تنظيف اسم القناة
     */
    private fun cleanChannelName(name: String): String {
        return name.replace(Regex("[^\\w\\s\\-\\[\\]()]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * توليد authorization token محسن
     */
    private fun generateEnhancedAuthorization(macAddress: String, token: String): String {
        val credentials = generateDeviceCredentials(macAddress)
        val authString = "${credentials.serialNumber}:$macAddress:$token"
        
        return Base64.getEncoder().encodeToString(authString.toByteArray())
    }
}

/**
 * بيانات اعتماد الجهاز
 */
data class DeviceCredentials(
    val mac: String,
    val serialNumber: String,
    val deviceId: String,
    val deviceId2: String,
    val signature: String,
    val stbType: String
)