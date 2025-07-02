package com.iptv.player.data.network

import com.iptv.player.data.model.Channel
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

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
    val status: Int
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

@Singleton
class StalkerService @Inject constructor(
    private val apiService: StalkerApiService
) {
    
    private var token: String? = null
    private var authorization: String? = null
    
    suspend fun initialize(portalUrl: String, macAddress: String): Boolean {
        try {
            // Perform handshake
            val handshakeResponse = apiService.handshake()
            if (handshakeResponse.isSuccessful) {
                token = handshakeResponse.body()?.js?.token
                
                // Create authorization token
                authorization = generateAuthorization(macAddress, token)
                
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    
    suspend fun getChannels(sourceId: Long): List<Channel> {
        if (authorization == null) return emptyList()
        
        try {
            val response = apiService.getOrderedList(authorization = authorization!!)
            if (response.isSuccessful) {
                return response.body()?.js?.data?.map { stalkerChannel ->
                    Channel(
                        id = stalkerChannel.id,
                        name = stalkerChannel.name,
                        group = null, // Will be filled from genres
                        logo = stalkerChannel.logo,
                        url = stalkerChannel.cmd,
                        epgId = stalkerChannel.xmltv_id,
                        sourceId = sourceId
                    )
                } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    private fun generateAuthorization(macAddress: String, token: String?): String {
        // Generate authorization token based on MAC address and token
        // This is a simplified version - actual implementation may vary
        return java.util.Base64.getEncoder().encodeToString(
            "$macAddress:$token".toByteArray()
        )
    }
}