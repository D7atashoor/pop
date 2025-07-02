package com.iptv.player.data.network

import android.util.Log
import com.google.gson.Gson
import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.*
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url
import java.net.URL
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
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
        private const val TAG = "StalkerService"
        
        // Portal types الشاملة - محسنة من الملفات المرفقة
        val PORTAL_ENDPOINTS = listOf(
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
            "/portalcc.php",
            "/api/v2/server/load.php",
            "/api/v3/server/load.php",
            "/stalker_portal/api/server/load.php",
            "/stb/portal.php",
            "/stb_portal/portal.php",
            "/mag_portal/portal.php",
            "/portal/server/load.php",
            "/stalker/portal.php",
            "/mini/portal.php",
            "/maxi/portal.php",
            "/premium/portal.php",
            "/vip/portal.php",
            "/gold/portal.php",
            "/silver/portal.php",
            "/bronze/portal.php"
        )
        
        // MAC Address Prefixes الشاملة للأجهزة المختلفة
        val MAC_PREFIXES = listOf(
            "00:1A:79",  // MAG 254/256/322/324/349/351
            "00:1B:3F",  // MAG 250/260/270
            "00:50:56",  // VMware
            "00:15:5D",  // Microsoft Hyper-V
            "08:00:27",  // VirtualBox
            "52:54:00",  // QEMU/KVM
            "00:0C:29",  // VMware Workstation
            "00:21:5A",  // MAG 200/245
            "00:13:CE",  // MAG devices
            "BC:76:70",  // MAG 351/352
            "84:DB:2F",  // MAG devices
            "E4:3E:D6",  // MAG devices
            "B4:E1:C4",  // Infomir devices
            "00:22:58",  // Infomir
            "AC:9B:0A",  // Formuler devices
            "08:EB:ED",  // X96 devices
            "74:23:44",  // Android TV boxes
            "1C:CC:D6",  // Nvidia Shield
            "B0:AC:13",  // Apple TV
            "F4:5C:89",  // Apple TV
            "28:CF:E9",  // Apple TV
            "A0:99:9B",  // Google devices
            "E8:EA:6A",  // Android boxes
            "54:27:1E",  // IPTV boxes
            "00:11:32",  // Synology
            "00:04:20",  // Slim devices
            "A4:02:B9",  // Xiaomi Mi Box
            "E0:DB:55",  // Amazon Fire TV
            "FC:65:DE",  // Amazon Fire TV
            "40:B0:76",  // Fire TV Stick
            "68:3E:34",  // Roku devices
            "D8:31:CF",  // Roku devices
            "B8:81:98",  // Roku devices
            "08:05:81",  // Samsung Smart TV
            "3C:BD:D8",  // Samsung Smart TV
            "54:BD:79",  // LG Smart TV
            "B8:86:87",  // LG Smart TV
            "00:7F:28",  // Generic STB
            "48:74:6E",  // Generic Android
            "D0:5F:B8",  // Generic devices
            "F0:EF:86",  // Generic devices
            "30:9C:23",  // Generic devices
            "78:11:DC",  // Generic devices
            "4C:CC:6A",  // Generic devices
            "2C:AB:00",  // Generic devices
            "70:85:C2",  // Generic devices
            "9C:04:EB",  // Generic devices
            "A8:1E:84",  // Generic devices
            "00:80:92"   // Generic devices
        )
        
        // Device Models المتقدمة
        val DEVICE_MODELS = mapOf(
            "00:1A:79" to "MAG254",
            "00:1B:3F" to "MAG250", 
            "00:21:5A" to "MAG200",
            "00:13:CE" to "MAG260",
            "BC:76:70" to "MAG351",
            "84:DB:2F" to "MAG322",
            "E4:3E:D6" to "MAG324",
            "B4:E1:C4" to "MAG256",
            "00:22:58" to "MAG349",
            "AC:9B:0A" to "Formuler Z8",
            "08:EB:ED" to "X96 Max",
            "74:23:44" to "Android TV",
            "1C:CC:D6" to "Nvidia Shield",
            "B0:AC:13" to "Apple TV 4K",
            "F4:5C:89" to "Apple TV HD", 
            "28:CF:E9" to "Apple TV 3rd",
            "A0:99:9B" to "Chromecast",
            "E8:EA:6A" to "Mi Box S",
            "54:27:1E" to "IPTV Box",
            "A4:02:B9" to "Mi Box 4",
            "E0:DB:55" to "Fire TV Stick 4K",
            "FC:65:DE" to "Fire TV Cube",
            "40:B0:76" to "Fire TV Stick",
            "68:3E:34" to "Roku Ultra",
            "D8:31:CF" to "Roku Express",
            "B8:81:98" to "Roku Premiere",
            "08:05:81" to "Samsung QLED",
            "3C:BD:D8" to "Samsung UHD",
            "54:BD:79" to "LG OLED",
            "B8:86:87" to "LG NanoCell"
        )
        
        // User Agents المتقدمة للأجهزة المختلفة
        val USER_AGENTS = mapOf(
            "MAG200" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stbapp ver: 2 rev: 250 Safari/533.3",
            "MAG245" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG245 stbapp ver: 2 rev: 1749 Safari/533.3",
            "MAG250" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG250 stbapp ver: 4 rev: 1812 Mobile Safari/533.3",
            "MAG254" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG254 stbapp ver: 4 rev: 2721 Mobile Safari/533.3",
            "MAG256" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG256 stbapp ver: 4 rev: 2796 Mobile Safari/533.3",
            "MAG260" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG260 stbapp ver: 4 rev: 1949 Mobile Safari/533.3",
            "MAG270" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG270 stbapp ver: 4 rev: 2721 Mobile Safari/533.3",
            "MAG322" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG322 stbapp ver: 4 rev: 2796 Mobile Safari/533.3",
            "MAG324" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG324 stbapp ver: 4 rev: 2796 Mobile Safari/533.3",
            "MAG349" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG349 stbapp ver: 4 rev: 2796 Mobile Safari/533.3",
            "MAG351" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG351 stbapp ver: 4 rev: 2796 Mobile Safari/533.3",
            "MAG352" to "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG352 stbapp ver: 4 rev: 2796 Mobile Safari/533.3",
            "AndroidTV" to "Dalvik/2.1.0 (Linux; U; Android 9; ADT-2 Build/PTT5.181126.002)",
            "AndroidBox" to "Mozilla/5.0 (Linux; Android 10; H96 MAX X3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36",
            "AppleTV5" to "AppleTV6,2/11.1",
            "AppleTV6" to "AppleTV11,1/11.1",
            "AppleTV7" to "AppleTV14,1/15.1.1",
            "FireTV" to "Mozilla/5.0 (Linux; Android 5.1; AFTS Build/LMY47O) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/41.99900.2250.0242 Safari/537.36",
            "FireTVStick" to "Mozilla/5.0 (Linux; Android 7.1.2; AFTMM Build/NS6265; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/70.0.3538.110 Mobile Safari/537.36",
            "PlayStation5" to "Mozilla/5.0 (PlayStation; PlayStation 5/2.26) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0 Safari/605.1.15",
            "Xbox" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; Xbox; Xbox Series X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36 Edge/20.02",
            "Roku" to "Roku/DVP-9.10 (559.10E04111A)",
            "ChromeCast" to "Mozilla/5.0 (CrKey armv7l 1.5.16041) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.0 Safari/537.36",
            "MiBox" to "Mozilla/5.0 (Linux; Android 9; Mi Box) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36",
            "NvidiaShield" to "Mozilla/5.0 (Linux; Android 9; SHIELD Android TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36",
            "SamsungTV" to "Mozilla/5.0 (SMART-TV; LINUX; Tizen 5.5) AppleWebKit/537.36 (KHTML, like Gecko) Version/5.5 TV Safari/537.36",
            "LGTV" to "Mozilla/5.0 (Web0S; Linux/SmartTV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Safari/537.36 WebAppManager",
            "CloudFlare" to "Mozilla/5.0 (compatible; CloudFlare-AlwaysOnline/1.0; +https://www.cloudflare.com/always-online) AppleWebKit/534.34 (KHTML, like Gecko) MAG200 stbapp ver: 2 rev: 250 Safari/534.34",
            "Windows" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "MacOS" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15",
            "Linux" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "iOS" to "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
            "Android" to "Mozilla/5.0 (Linux; Android 11; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Mobile Safari/537.36"
        )
        
        // Device Capabilities
        val DEVICE_CAPABILITIES = mapOf(
            "MAG254" to mapOf(
                "max_resolution" to "1080p",
                "audio_codecs" to listOf("AAC", "MP3", "AC3"),
                "video_codecs" to listOf("H.264", "MPEG-2", "MPEG-4"),
                "supports_4k" to false,
                "supports_hevc" to false,
                "supports_hdr" to false
            ),
            "MAG256" to mapOf(
                "max_resolution" to "1080p",
                "audio_codecs" to listOf("AAC", "MP3", "AC3", "DTS"),
                "video_codecs" to listOf("H.264", "H.265", "MPEG-2", "MPEG-4"),
                "supports_4k" to false,
                "supports_hevc" to true,
                "supports_hdr" to false
            ),
            "MAG322" to mapOf(
                "max_resolution" to "1080p",
                "audio_codecs" to listOf("AAC", "MP3", "AC3", "DTS"),
                "video_codecs" to listOf("H.264", "H.265", "MPEG-2", "MPEG-4"),
                "supports_4k" to false,
                "supports_hevc" to true,
                "supports_hdr" to false
            ),
            "MAG324" to mapOf(
                "max_resolution" to "1080p",
                "audio_codecs" to listOf("AAC", "MP3", "AC3", "DTS"),
                "video_codecs" to listOf("H.264", "H.265", "MPEG-2", "MPEG-4", "VP9"),
                "supports_4k" to false,
                "supports_hevc" to true,
                "supports_hdr" to false
            ),
            "MAG349" to mapOf(
                "max_resolution" to "4K",
                "audio_codecs" to listOf("AAC", "MP3", "AC3", "DTS", "Dolby Digital+"),
                "video_codecs" to listOf("H.264", "H.265", "VP9", "AV1"),
                "supports_4k" to true,
                "supports_hevc" to true,
                "supports_hdr" to true
            ),
            "MAG351" to mapOf(
                "max_resolution" to "4K",
                "audio_codecs" to listOf("AAC", "MP3", "AC3", "DTS", "Dolby Digital+", "Dolby Atmos"),
                "video_codecs" to listOf("H.264", "H.265", "VP9", "AV1"),
                "supports_4k" to true,
                "supports_hevc" to true,
                "supports_hdr" to true
            )
        )
        
        // Time zones with country flags
        val TIMEZONE_MAPPINGS = mapOf(
            "UTC" to "🌍 UTC",
            "Europe/London" to "🇬🇧 United Kingdom",
            "Europe/Paris" to "🇫🇷 France",
            "Europe/Berlin" to "🇩🇪 Germany",
            "Europe/Rome" to "🇮🇹 Italy",
            "Europe/Madrid" to "🇪🇸 Spain",
            "Europe/Amsterdam" to "🇳🇱 Netherlands",
            "Europe/Brussels" to "🇧🇪 Belgium",
            "Europe/Vienna" to "🇦🇹 Austria",
            "Europe/Warsaw" to "🇵🇱 Poland",
            "Europe/Prague" to "🇨🇿 Czech Republic",
            "Europe/Budapest" to "🇭🇺 Hungary",
            "Europe/Bucharest" to "🇷🇴 Romania",
            "Europe/Sofia" to "🇧🇬 Bulgaria",
            "Europe/Athens" to "🇬🇷 Greece",
            "Europe/Helsinki" to "🇫🇮 Finland",
            "Europe/Stockholm" to "🇸🇪 Sweden",
            "Europe/Oslo" to "🇳🇴 Norway",
            "Europe/Copenhagen" to "🇩🇰 Denmark",
            "Europe/Istanbul" to "🇹🇷 Turkey",
            "Europe/Moscow" to "🇷🇺 Russia",
            "Europe/Kiev" to "🇺🇦 Ukraine",
            "America/New_York" to "🇺🇸 United States (EST)",
            "America/Los_Angeles" to "🇺🇸 United States (PST)",
            "America/Chicago" to "🇺🇸 United States (CST)",
            "America/Denver" to "🇺🇸 United States (MST)",
            "America/Toronto" to "🇨🇦 Canada",
            "America/Mexico_City" to "🇲🇽 Mexico",
            "America/Sao_Paulo" to "🇧🇷 Brazil",
            "America/Buenos_Aires" to "🇦🇷 Argentina",
            "Asia/Dubai" to "🇦🇪 UAE",
            "Asia/Riyadh" to "🇸🇦 Saudi Arabia",
            "Asia/Kuwait" to "🇰🇼 Kuwait",
            "Asia/Doha" to "🇶🇦 Qatar",
            "Asia/Muscat" to "🇴🇲 Oman",
            "Asia/Manama" to "🇧🇭 Bahrain",
            "Asia/Baghdad" to "🇮🇶 Iraq",
            "Asia/Tehran" to "🇮🇷 Iran",
            "Asia/Karachi" to "🇵🇰 Pakistan",
            "Asia/Kabul" to "🇦🇫 Afghanistan",
            "Asia/Kolkata" to "🇮🇳 India",
            "Asia/Dhaka" to "🇧🇩 Bangladesh",
            "Asia/Kathmandu" to "🇳🇵 Nepal",
            "Asia/Colombo" to "🇱🇰 Sri Lanka",
            "Asia/Bangkok" to "🇹🇭 Thailand",
            "Asia/Jakarta" to "🇮🇩 Indonesia",
            "Asia/Singapore" to "🇸🇬 Singapore",
            "Asia/Kuala_Lumpur" to "🇲🇾 Malaysia",
            "Asia/Manila" to "🇵🇭 Philippines",
            "Asia/Shanghai" to "🇨🇳 China",
            "Asia/Hong_Kong" to "🇭🇰 Hong Kong",
            "Asia/Taipei" to "🇹🇼 Taiwan",
            "Asia/Tokyo" to "🇯🇵 Japan",
            "Asia/Seoul" to "🇰🇷 South Korea",
            "Australia/Sydney" to "🇦🇺 Australia (EST)",
            "Australia/Perth" to "🇦🇺 Australia (WST)",
            "Australia/Melbourne" to "🇦🇺 Australia (AEST)",
            "Pacific/Auckland" to "🇳🇿 New Zealand",
            "Africa/Cairo" to "🇪🇬 Egypt",
            "Africa/Lagos" to "🇳🇬 Nigeria",
            "Africa/Casablanca" to "🇲🇦 Morocco",
            "Africa/Tunis" to "🇹🇳 Tunisia",
            "Africa/Algiers" to "🇩🇿 Algeria",
            "Africa/Johannesburg" to "🇿🇦 South Africa"
        )
    }
    
    private var token: String? = null
    private var authorization: String? = null
    private var macAddress: String? = null
    
    private val httpClient = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(java.net.CookieManager()))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * التحقق المتقدم من صحة مصدر Stalker/MAC Portal.
     */
    suspend fun validateSourceAdvanced(
        url: String,
        macAddress: String
    ): StalkerValidationDetails = withContext(Dispatchers.IO) {
        val normalizedMac = normalizeMacAddress(macAddress)
        if (normalizedMac == null) {
            Log.e(TAG, "عنوان MAC غير صالح: $macAddress")
            return@withContext StalkerValidationDetails(isValid = false, error = "Invalid MAC Address")
        }

        val baseUrl = getBaseUrl(url)
        val host = URL(baseUrl).host
        val port = URL(baseUrl).port

        val portalEndpoints = listOf(
            "$baseUrl/portal.php",
            "$baseUrl/stalker_portal/server/load.php",
            "$baseUrl/c/"
        )

        for (attempt in 1..2) { // Retry logic
            for (portalUrl in portalEndpoints) {
                try {
                    val result = checkStalkerPortal(portalUrl, normalizedMac, host, port)
                    if (result.isValid) {
                        return@withContext result
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "فشل التحقق من $portalUrl في المحاولة $attempt", e)
                }
            }
            if (attempt == 1) {
                delay(1000) // انتظر قبل إعادة المحاولة
            }
        }

        return@withContext StalkerValidationDetails(isValid = false, error = "Failed to validate with all endpoints")
    }

    private suspend fun checkStalkerPortal(
        portalBaseUrl: String,
        mac: String,
        host: String,
        port: Int
    ): StalkerValidationDetails {
        val session = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(java.net.CookieManager()))
            .connectTimeout(10, TimeUnit.SECONDS).build()

        // 1. Handshake
        val handshakeUrl = "$portalBaseUrl?type=stb&action=handshake&token=&JsHttpRequest=1-xml"
        val handshakeHeaders = buildStalkerHeaders(mac, host, port, portalBaseUrl)
        
        val handshakeResponse = executeRequest(session, handshakeUrl, handshakeHeaders)
        val token = parseToken(handshakeResponse) ?: return StalkerValidationDetails(isValid = false)
        Log.d(TAG, "Handshake successful for $portalBaseUrl, token received.")

        // 2. Get Profile
        val profileUrl = "$portalBaseUrl?type=stb&action=get_profile&JsHttpRequest=1-xml"
        val authHeaders = handshakeHeaders.newBuilder().add("Authorization", "Bearer $token").build()
        val profileResponse = executeRequest(session, profileUrl, authHeaders)
        val timezone = parseTimezone(profileResponse)
        Log.d(TAG, "Profile data received, timezone: $timezone")

        // 3. Get Account Info
        val accountUrl = "$portalBaseUrl?type=account_info&action=get_main_info&JsHttpRequest=1-xml"
        val accountResponse = executeRequest(session, accountUrl, authHeaders)
        return parseAccountInfo(accountResponse, portalBaseUrl, timezone)
    }

    private suspend fun executeRequest(session: OkHttpClient, url: String, headers: Headers): String? {
        return withTimeoutOrNull(10000) {
            val request = Request.Builder().url(url).headers(headers).build()
            session.newCall(request).execute().body?.string()
        }
    }
    
    private fun parseToken(response: String?): String? {
        return response?.let {
            try {
                JSONObject(it).getJSONObject("js").getString("token")
            } catch (e: Exception) { null }
        }
    }

    private fun parseTimezone(response: String?): String? {
        return response?.let {
            try {
                JSONObject(it).getJSONObject("js").getString("default_timezone")
            } catch (e: Exception) { null }
        }
    }

    private fun parseAccountInfo(response: String?, portalUrl: String, timezone: String?): StalkerValidationDetails {
        if (response == null || !response.contains("\"js\":{\"mac\"")) {
            return StalkerValidationDetails(isValid = false, error = "Invalid account info response")
        }
        try {
            val js = JSONObject(response).getJSONObject("js")
            val status = js.optString("status", js.optString("account_status", "Active"))
            
            return StalkerValidationDetails(
                isValid = true,
                portalUrl = portalUrl,
                macAddress = js.getString("mac"),
                expiryDate = js.optString("phone", js.optString("exp_date", "N/A")),
                status = status,
                timezone = timezone
            )
        } catch (e: Exception) {
            return StalkerValidationDetails(isValid = false, error = "Failed to parse account info")
        }
    }
    
    fun normalizeMacAddress(mac: String): String? {
        val cleanMac = mac.uppercase(Locale.ROOT).replace(Regex("[^0-9A-F:]"), "")
        val hexChars = cleanMac.replace(":", "")
        
        if (hexChars.length < 12) return null // Not enough characters
        val finalHex = hexChars.substring(0, 12)
        
        return (0 until 12 step 2).joinToString(":") { finalHex.substring(it, it + 2) }
    }
    
    private fun getBaseUrl(url: String): String {
        var server = url.trim().let { if (it.endsWith('/')) it.dropLast(1) else it }
        if (!server.startsWith("http")) {
            server = "http://$server"
        }
        return server
    }

    private fun buildStalkerHeaders(mac: String, host: String, port: Int, refererUrl: String): Headers {
        return Headers.Builder()
            .add("User-Agent", "Lavf53.32.100")
            .add("Pragma", "no-cache")
            .add("Accept", "*/*")
            .add("Referer", "$refererUrl/c/index.html")
            .add("X-User-Agent", "Model: MAG250; Link: WiFi")
            .add("Host", "$host:$port")
            .add("Cookie", "mac=$mac; stb_lang=en; timezone=Europe/Paris")
            .add("Connection", "Close")
            .add("Accept-Encoding", "gzip, deflate")
            .build()
    }
    
    /**
     * توليد عنوان MAC عشوائي مع prefix محدد
     */
    fun generateMACAddress(prefix: String? = null): String {
        val selectedPrefix = prefix ?: MAC_PREFIXES.random()
        val randomBytes = (1..3).map { 
            "%02X".format((0..255).random()) 
        }.joinToString(":")
        
        Log.d(TAG, "توليد MAC بـ prefix: $selectedPrefix")
        return "$selectedPrefix:$randomBytes"
    }
    
    /**
     * التحقق من صحة عنوان MAC
     */
    fun isValidMAC(mac: String): Boolean {
        val macPattern = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
        return macPattern.matches(mac)
    }
    
    /**
     * توليد Device Credentials كاملة
     */
    fun generateDeviceCredentials(macAddress: String): Map<String, String> {
        val deviceModel = getDeviceModelFromMAC(macAddress)
        val userAgent = getUserAgentForDevice(deviceModel)
        
        val serialNumber = generateSerialNumber(deviceModel)
        val deviceId = generateDeviceId(macAddress)
        val signature = generateDeviceSignature(macAddress, deviceModel)
        
        Log.d(TAG, "توليد Device Credentials لـ: $deviceModel")
        
        return mapOf(
            "mac" to macAddress,
            "device_model" to deviceModel,
            "user_agent" to userAgent,
            "serial_number" to serialNumber,
            "device_id" to deviceId,
            "device_signature" to signature,
            "firmware_version" to getFirmwareVersion(deviceModel),
            "hardware_version" to getHardwareVersion(deviceModel),
            "timezone" to getRandomTimezone(),
            "language" to "en",
            "country" to "US"
        )
    }
    
    /**
     * الحصول على نموذج الجهاز من MAC Address
     */
    private fun getDeviceModelFromMAC(macAddress: String): String {
        val prefix = macAddress.substring(0, 8)
        return DEVICE_MODELS[prefix] ?: "MAG254"
    }
    
    /**
     * الحصول على User Agent للجهاز
     */
    private fun getUserAgentForDevice(deviceModel: String): String {
        return USER_AGENTS[deviceModel] ?: USER_AGENTS["MAG254"]!!
    }
    
    /**
     * توليد Serial Number للجهاز
     */
    private fun generateSerialNumber(deviceModel: String): String {
        val prefix = when (deviceModel) {
            "MAG254" -> "254"
            "MAG256" -> "256"
            "MAG322" -> "322"
            "MAG324" -> "324"
            "MAG349" -> "349"
            "MAG351" -> "351"
            else -> "254"
        }
        
        val randomDigits = (1..8).map { (0..9).random() }.joinToString("")
        return "$prefix$randomDigits"
    }
    
    /**
     * توليد Device ID
     */
    private fun generateDeviceId(macAddress: String): String {
        return macAddress.replace(":", "").lowercase() + 
               (1..4).map { (0..9).random() }.joinToString("")
    }
    
    /**
     * توليد Device Signature
     */
    private fun generateDeviceSignature(macAddress: String, deviceModel: String): String {
        val base = "$macAddress$deviceModel${System.currentTimeMillis()}"
        return base.hashCode().toString().replace("-", "")
    }
    
    /**
     * الحصول على إصدار البرنامج الثابت
     */
    private fun getFirmwareVersion(deviceModel: String): String {
        return when (deviceModel) {
            "MAG254" -> "v4.2.721"
            "MAG256" -> "v4.3.796" 
            "MAG322" -> "v4.3.796"
            "MAG324" -> "v4.3.796"
            "MAG349" -> "v4.4.901"
            "MAG351" -> "v4.4.901"
            else -> "v4.2.721"
        }
    }
    
    /**
     * الحصول على إصدار الهاردوير
     */
    private fun getHardwareVersion(deviceModel: String): String {
        return when (deviceModel) {
            "MAG254" -> "1.0-BD-12"
            "MAG256" -> "2.0-BD-15"
            "MAG322" -> "1.0-BD-18"
            "MAG324" -> "1.0-BD-20"
            "MAG349" -> "1.0-BD-25"
            "MAG351" -> "1.0-BD-27"
            else -> "1.0-BD-12"
        }
    }
    
    /**
     * الحصول على منطقة زمنية عشوائية
     */
    private fun getRandomTimezone(): String {
        return TIMEZONE_MAPPINGS.keys.random()
    }
    
    /**
     * الحصول على معلومات Timezone مع العلم
     */
    fun getTimezoneWithFlag(timezone: String): String {
        return TIMEZONE_MAPPINGS[timezone] ?: "🌍 $timezone"
    }
    
    /**
     * الحصول على قدرات الجهاز
     */
    fun getDeviceCapabilities(deviceModel: String): Map<String, Any>? {
        return DEVICE_CAPABILITIES[deviceModel]
    }
    
    /**
     * التحقق من دعم ميزة معينة
     */
    fun supportsFeature(deviceModel: String, feature: String): Boolean {
        val capabilities = getDeviceCapabilities(deviceModel) ?: return false
        return when (feature) {
            "4k" -> capabilities["supports_4k"] as? Boolean ?: false
            "hevc" -> capabilities["supports_hevc"] as? Boolean ?: false
            "hdr" -> capabilities["supports_hdr"] as? Boolean ?: false
            else -> false
        }
    }
    
    /**
     * الحصول على أفضل user agent للحماية من CloudFlare
     */
    fun getCloudFlareUserAgent(): String {
        return USER_AGENTS["CloudFlare"]!!
    }
    
    /**
     * الحصول على user agent عشوائي
     */
    fun getRandomUserAgent(): String {
        return USER_AGENTS.values.random()
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
        val authString = "${credentials["serial_number"]}:$macAddress:$token"
        
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

data class StalkerValidationDetails(
    val isValid: Boolean,
    val portalUrl: String? = null,
    val macAddress: String? = null,
    val expiryDate: String? = null,
    val status: String? = null,
    val timezone: String? = null,
    val error: String? = null
)