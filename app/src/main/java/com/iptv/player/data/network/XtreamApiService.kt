package com.iptv.player.data.network

import com.iptv.player.data.model.Channel
import com.iptv.player.data.model.Movie
import com.iptv.player.data.model.Series
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

interface XtreamApiService {
    
    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<XtreamAuthResponse>
    
    @GET("player_api.php")
    suspend fun getCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
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
    val allowed_output_formats: List<String>
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
    val parent_id: Int
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
    val custom_sid: String,
    val tv_archive: Int,
    val direct_source: String,
    val tv_archive_duration: Int
)

data class XtreamMovie(
    val num: Int,
    val name: String,
    val stream_type: String,
    val stream_id: Int,
    val stream_icon: String,
    val rating: String,
    val rating_5based: Double,
    val added: String,
    val category_id: String,
    val container_extension: String,
    val custom_sid: String,
    val direct_source: String
)

data class XtreamSeries(
    val num: Int,
    val name: String,
    val series_id: Int,
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

@Singleton
class XtreamService @Inject constructor(
    private val apiService: XtreamApiService
) {
    
    suspend fun getChannels(
        serverUrl: String,
        username: String,
        password: String,
        sourceId: Long
    ): List<Channel> {
        try {
            val response = apiService.getLiveStreams(username, password)
            if (response.isSuccessful) {
                return response.body()?.map { xtreamChannel ->
                    Channel(
                        id = xtreamChannel.stream_id.toString(),
                        name = xtreamChannel.name,
                        group = null, // Will be filled from categories
                        logo = xtreamChannel.stream_icon,
                        url = buildStreamUrl(serverUrl, username, password, xtreamChannel.stream_id, "live"),
                        epgId = xtreamChannel.epg_channel_id,
                        sourceId = sourceId
                    )
                } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    private fun buildStreamUrl(
        serverUrl: String,
        username: String,
        password: String,
        streamId: Int,
        type: String
    ): String {
        return "$serverUrl/$type/$username/$password/$streamId.m3u8"
    }
}