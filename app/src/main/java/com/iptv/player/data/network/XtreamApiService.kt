package com.iptv.player.data.network

import com.iptv.player.data.model.Channel
import com.iptv.player.data.model.Movie
import com.iptv.player.data.model.Series
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface XtreamApiService {
    
    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<XtreamAuthResponse>
    
    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): Response<List<XtreamCategory>>
    
    @GET("player_api.php")
    suspend fun getVODCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): Response<List<XtreamCategory>>
    
    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): Response<List<XtreamCategory>>
    
    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String? = null
    ): Response<List<XtreamChannel>>
    
    @GET("player_api.php")
    suspend fun getVODStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String? = null
    ): Response<List<XtreamMovie>>
    
    @GET("player_api.php")
    suspend fun getSeriesStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series",
        @Query("category_id") categoryId: String? = null
    ): Response<List<XtreamSeries>>
    
    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: String
    ): Response<XtreamSeriesInfo>
    
    @GET("player_api.php")
    suspend fun getVODInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_info",
        @Query("vod_id") vodId: String
    ): Response<XtreamVODInfo>
    
    @GET("player_api.php")
    suspend fun getShortEPG(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_short_epg",
        @Query("stream_id") streamId: String,
        @Query("limit") limit: Int = 100
    ): Response<Map<String, XtreamEPGProgram>>
}

data class XtreamAuthResponse(
    val user_info: XtreamUserInfo,
    val server_info: XtreamServerInfo
)

data class XtreamUserInfo(
    val username: String,
    val password: String,
    val message: String,
    val auth: Int,
    val status: String,
    val exp_date: String,
    val is_trial: String,
    val active_cons: String,
    val created_at: String,
    val max_connections: String,
    val allowed_output_formats: List<String>? = null
)

data class XtreamServerInfo(
    val url: String,
    val port: String,
    val https_port: String,
    val server_protocol: String,
    val rtmp_port: String,
    val timezone: String,
    val timestamp_now: Long,
    val time_now: String
)

data class XtreamCategory(
    val category_id: String,
    val category_name: String,
    val parent_id: Int? = null
)

data class XtreamChannel(
    val num: Int,
    val name: String,
    val stream_type: String,
    val stream_id: Int,
    val stream_icon: String,
    val epg_channel_id: String,
    val added: String,
    val category_id: String,
    val custom_sid: String? = null,
    val tv_archive: Int? = null,
    val direct_source: String? = null,
    val tv_archive_duration: Int? = null
)

data class XtreamMovie(
    val num: Int,
    val name: String,
    val stream_type: String,
    val stream_id: Int,
    val stream_icon: String,
    val rating: String? = null,
    val rating_5based: Double? = null,
    val added: String,
    val category_id: String,
    val container_extension: String,
    val custom_sid: String? = null,
    val direct_source: String? = null
)

data class XtreamSeries(
    val num: Int,
    val name: String,
    val series_id: Int,
    val cover: String,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    val releaseDate: String? = null,
    val last_modified: String,
    val rating: String? = null,
    val rating_5based: Double? = null,
    val backdrop_path: List<String>? = null,
    val youtube_trailer: String? = null,
    val episode_run_time: String? = null,
    val category_id: String
)

data class XtreamSeriesInfo(
    val seasons: List<XtreamSeason>,
    val info: XtreamSeriesDetails,
    val episodes: Map<String, List<XtreamEpisode>>
)

data class XtreamSeason(
    val air_date: String,
    val episode_count: Int,
    val id: Int,
    val name: String,
    val overview: String,
    val poster_path: String,
    val season_number: Int
)

data class XtreamSeriesDetails(
    val name: String,
    val cover: String,
    val plot: String,
    val cast: String,
    val director: String,
    val genre: String,
    val releaseDate: String,
    val last_modified: String,
    val rating: String,
    val rating_5based: Double,
    val backdrop_path: List<String>,
    val youtube_trailer: String,
    val episode_run_time: String,
    val category_id: String
)

data class XtreamEpisode(
    val id: String,
    val episode_num: Int,
    val title: String,
    val container_extension: String,
    val info: XtreamEpisodeInfo,
    val custom_sid: String,
    val added: String,
    val season: Int,
    val direct_source: String
)

data class XtreamEpisodeInfo(
    val air_date: String,
    val crew: String,
    val rating: Double,
    val id: Int,
    val movie_image: String,
    val name: String,
    val overview: String,
    val season_number: Int,
    val episode_number: Int,
    val still_path: String
)

data class XtreamVODInfo(
    val info: XtreamVODDetails,
    val movie_data: XtreamMovieData
)

data class XtreamVODDetails(
    val kinopoisk_url: String,
    val tmdb_id: Int,
    val name: String,
    val o_name: String,
    val cover_big: String,
    val movie_image: String,
    val releasedate: String,
    val episode_run_time: Int,
    val youtube_trailer: String,
    val director: String,
    val actors: String,
    val cast: String,
    val description: String,
    val plot: String,
    val age: String,
    val mpaa_rating: String,
    val rating_5based: Double,
    val country: String,
    val genre: String,
    val backdrop_path: List<String>,
    val duration_secs: Int,
    val duration: String,
    val video: Map<String, String>,
    val audio: Map<String, String>,
    val bitrate: Int
)

data class XtreamMovieData(
    val stream_id: Int,
    val name: String,
    val added: String,
    val category_id: String,
    val container_extension: String,
    val custom_sid: String,
    val direct_source: String
)

data class XtreamEPGProgram(
    val id: String,
    val epg_id: String,
    val title: String,
    val lang: String,
    val start: String,
    val end: String,
    val description: String,
    val channel_id: String,
    val start_timestamp: Long,
    val stop_timestamp: Long
)

@Singleton
class XtreamService @Inject constructor(
    private val apiService: XtreamApiService
) {
    
    /**
     * التحقق من صحة بيانات الاعتماد
     */
    suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): XtreamAuthResponse? {
        try {
            val response = apiService.authenticate(username, password)
            if (response.isSuccessful) {
                val authData = response.body()
                // التحقق من صحة الاستجابة
                if (authData?.user_info?.auth == 1) {
                    return authData
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    /**
     * فحص انتهاء الحساب
     */
    fun isAccountExpired(userInfo: XtreamUserInfo): Boolean {
        return try {
            if (userInfo.exp_date == "null" || userInfo.exp_date.isEmpty()) {
                false // حساب غير محدود
            } else {
                val expTimestamp = userInfo.exp_date.toLongOrNull() ?: return false
                val currentTime = System.currentTimeMillis() / 1000
                expTimestamp < currentTime
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * حساب الأيام المتبقية للحساب
     */
    fun getDaysRemaining(userInfo: XtreamUserInfo): Int {
        return try {
            if (userInfo.exp_date == "null" || userInfo.exp_date.isEmpty()) {
                -1 // غير محدود
            } else {
                val expTimestamp = userInfo.exp_date.toLongOrNull() ?: return 0
                val currentTime = System.currentTimeMillis() / 1000
                val secondsRemaining = expTimestamp - currentTime
                if (secondsRemaining > 0) {
                    (secondsRemaining / (24 * 60 * 60)).toInt()
                } else {
                    0
                }
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * تنسيق تاريخ انتهاء الحساب
     */
    fun formatExpiryDate(userInfo: XtreamUserInfo): String {
        return try {
            if (userInfo.exp_date == "null" || userInfo.exp_date.isEmpty()) {
                "Unlimited"
            } else {
                val timestamp = userInfo.exp_date.toLongOrNull() ?: return "Invalid"
                val date = Date(timestamp * 1000)
                val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                formatter.format(date)
            }
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
    
    /**
     * الحصول على القنوات المباشرة مع تحسينات
     */
    suspend fun getChannels(
        serverUrl: String,
        username: String,
        password: String,
        sourceId: Long,
        categoryId: String? = null
    ): List<Channel> {
        try {
            val response = apiService.getLiveStreams(username, password, categoryId = categoryId)
            if (response.isSuccessful) {
                return response.body()?.map { xtreamChannel ->
                    Channel(
                        id = xtreamChannel.stream_id.toString(),
                        name = cleanChannelName(xtreamChannel.name),
                        group = null, // سيتم ملؤها من الفئات
                        logo = xtreamChannel.stream_icon.takeIf { it.isNotEmpty() },
                        url = buildStreamUrl(serverUrl, username, password, xtreamChannel.stream_id, "live"),
                        epgId = xtreamChannel.epg_channel_id.takeIf { it.isNotEmpty() },
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
     * الحصول على الأفلام مع تحسينات
     */
    suspend fun getMovies(
        serverUrl: String,
        username: String,
        password: String,
        sourceId: Long,
        categoryId: String? = null
    ): List<Movie> {
        try {
            val response = apiService.getVODStreams(username, password, categoryId = categoryId)
            if (response.isSuccessful) {
                return response.body()?.map { xtreamMovie ->
                    Movie(
                        id = xtreamMovie.stream_id.toString(),
                        name = xtreamMovie.name,
                        description = null, // سيتم الحصول عليها من تفاصيل الفيلم
                        poster = xtreamMovie.stream_icon.takeIf { it.isNotEmpty() },
                        year = extractYearFromName(xtreamMovie.name),
                        genre = null, // سيتم الحصول عليها من التفاصيل
                        url = buildStreamUrl(serverUrl, username, password, xtreamMovie.stream_id, "movie", xtreamMovie.container_extension),
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
     * الحصول على المسلسلات مع تحسينات
     */
    suspend fun getSeries(
        serverUrl: String,
        username: String,
        password: String,
        sourceId: Long,
        categoryId: String? = null
    ): List<Series> {
        try {
            val response = apiService.getSeriesStreams(username, password, categoryId = categoryId)
            if (response.isSuccessful) {
                return response.body()?.map { xtreamSeries ->
                    Series(
                        id = xtreamSeries.series_id.toString(),
                        name = xtreamSeries.name,
                        description = xtreamSeries.plot,
                        poster = xtreamSeries.cover.takeIf { it.isNotEmpty() },
                        year = extractYearFromDate(xtreamSeries.releaseDate),
                        genre = xtreamSeries.genre,
                        episodes = emptyList(), // سيتم تحميلها عند الحاجة
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
     * الحصول على فئات القنوات المباشرة
     */
    suspend fun getLiveCategories(username: String, password: String): List<XtreamCategory> {
        try {
            val response = apiService.getLiveCategories(username, password)
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    /**
     * الحصول على فئات الأفلام
     */
    suspend fun getVODCategories(username: String, password: String): List<XtreamCategory> {
        try {
            val response = apiService.getVODCategories(username, password)
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    /**
     * الحصول على فئات المسلسلات
     */
    suspend fun getSeriesCategories(username: String, password: String): List<XtreamCategory> {
        try {
            val response = apiService.getSeriesCategories(username, password)
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    /**
     * توليد رابط M3U
     */
    fun generateM3UUrl(serverUrl: String, username: String, password: String): String {
        return "$serverUrl/get.php?username=$username&password=$password&type=m3u_plus"
    }
    
    /**
     * توليد رابط XMLTV للـ EPG
     */
    fun generateXMLTVUrl(serverUrl: String, username: String, password: String): String {
        return "$serverUrl/xmltv.php?username=$username&password=$password"
    }
    
    /**
     * الحصول على معلومات تفصيلية للمسلسل
     */
    suspend fun getSeriesInfo(
        username: String,
        password: String,
        seriesId: String
    ): XtreamSeriesInfo? {
        try {
            val response = apiService.getSeriesInfo(username, password, seriesId = seriesId)
            if (response.isSuccessful) {
                return response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    /**
     * الحصول على معلومات تفصيلية للفيلم
     */
    suspend fun getVODInfo(
        username: String,
        password: String,
        vodId: String
    ): XtreamVODInfo? {
        try {
            val response = apiService.getVODInfo(username, password, vodId = vodId)
            if (response.isSuccessful) {
                return response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    /**
     * بناء رابط البث
     */
    private fun buildStreamUrl(
        serverUrl: String,
        username: String,
        password: String,
        streamId: Int,
        type: String,
        extension: String = "m3u8"
    ): String {
        return when (type) {
            "live" -> "$serverUrl/live/$username/$password/$streamId.$extension"
            "movie" -> "$serverUrl/movie/$username/$password/$streamId.$extension"
            "series" -> "$serverUrl/series/$username/$password/$streamId.$extension"
            else -> "$serverUrl/$type/$username/$password/$streamId.$extension"
        }
    }
    
    /**
     * تنظيف اسم القناة/الفيلم
     */
    private fun cleanChannelName(name: String): String {
        return name.replace(Regex("[^\\w\\s\\-\\[\\]()]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * استخراج السنة من الاسم
     */
    private fun extractYearFromName(name: String): Int? {
        val yearPattern = Regex("\\((\\d{4})\\)")
        val match = yearPattern.find(name)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }
    
    /**
     * استخراج السنة من التاريخ
     */
    private fun extractYearFromDate(dateString: String?): Int? {
        if (dateString == null) return null
        return try {
            dateString.substring(0, 4).toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * تصنيف القناة حسب الاسم
     */
    fun categorizeChannelByName(channelName: String): String {
        val name = channelName.lowercase()
        
        return when {
            name.contains("sport") || name.contains("espn") || name.contains("bein") || 
            name.contains("fox sport") || name.contains("sky sport") -> "Sports"
            
            name.contains("news") || name.contains("cnn") || name.contains("bbc") || 
            name.contains("fox news") || name.contains("al jazeera") -> "News"
            
            name.contains("movie") || name.contains("cinema") || name.contains("film") || 
            name.contains("hollywood") -> "Movies"
            
            name.contains("kids") || name.contains("cartoon") || name.contains("disney") || 
            name.contains("nickelodeon") -> "Kids"
            
            name.contains("music") || name.contains("mtv") || name.contains("vh1") -> "Music"
            
            name.contains("hd") || name.contains("4k") || name.contains("uhd") -> "HD"
            
            else -> "General"
        }
    }
}