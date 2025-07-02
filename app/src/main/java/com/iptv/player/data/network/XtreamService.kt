package com.iptv.player.data.network

import android.util.Log
import com.google.gson.Gson
import com.iptv.player.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamService @Inject constructor() {

    companion object {
        private const val TAG = "XtreamService"
        
        // Endpoints متعددة للتحقق
        val API_ENDPOINTS = listOf("/player_api.php", "/panel_api.php", "/api.php")
        
        // صيغ M3U متعددة للتحقق كخيار احتياطي
        val M3U_URL_FORMATS = listOf(
            "/get.php?username={user}&password={pass}&type=m3u_plus&output=ts",
            "/get.php?username={user}&password={pass}&type=m3u",
            "/live/{user}/{pass}/",
            "/{user}/{pass}/all.m3u"
        )
    }
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
        
    private val apiService: XtreamApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/") // سيتم استبدالها بـ @Url
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamApiService::class.java)
    }

    /**
     * التحقق المتقدم من صحة مصدر Xtream Codes.
     */
    suspend fun validateSource(
        url: String,
        username: String,
        password: String
    ): XtreamValidationDetails = withContext(Dispatchers.IO) {
        val cleanUrl = cleanXtreamUrl(url)
        Log.d(TAG, "بدء التحقق المتقدم من Xtream لـ: $cleanUrl")

        val headers = Headers.Builder()
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .add("Accept", "application/json, text/plain, */*")
            .add("Connection", "keep-alive")
            .build()

        for (endpoint in API_ENDPOINTS) {
            val requestUrl = "$cleanUrl$endpoint?username=$username&password=$password"
            try {
                val request = Request.Builder().url(requestUrl).headers(headers).build()
                val response = withTimeoutOrNull(7000) { httpClient.newCall(request).execute() }

                response?.use {
                    if (!it.isSuccessful) return@use

                    val responseBody = it.body?.string() ?: ""
                    var data: JSONObject? = null
                    
                    try {
                        data = JSONObject(responseBody)
                    } catch (e: Exception) {
                        val jsonMatch = Pattern.compile("(\\{.*\\})").matcher(responseBody)
                        if (jsonMatch.find()) {
                            try {
                                data = JSONObject(jsonMatch.group(1)!!)
                            } catch (e2: Exception) {
                                Log.w(TAG, "Regex found JSON, but failed to parse for $requestUrl")
                            }
                        }
                    }

                    if (data == null) return@use
                    
                    val userInfo = data.optJSONObject("user_info") ?: JSONObject()
                    val serverInfo = data.optJSONObject("server_info") ?: JSONObject()

                    val isActive = userInfo.optString("status") == "Active"
                    val isAuthed = userInfo.optInt("auth", 0) == 1 || data.optInt("auth", 0) == 1
                    val hasServerInfo = data.has("server_info")
                    
                    if (isActive || isAuthed || hasServerInfo) {
                        Log.d(TAG, "تم التحقق من حساب Xtream بنجاح عبر $endpoint")
                        return@withContext parseSuccessfulXtreamResponse(data, cleanUrl, username, password, endpoint)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في اختبار Xtream endpoint $endpoint", e)
            }
        }
        
        Log.w(TAG, "فشلت جميع endpoints، جاري تجربة صيغ M3U كخيار احتياطي...")
        for (format in M3U_URL_FORMATS) {
            val m3uUrl = buildTestUrl(cleanUrl, format, username, password)
            if (testUrlAccess(m3uUrl)) {
                 Log.d(TAG, "تم العثور على رابط M3U صالح: $m3uUrl")
                 return@withContext XtreamValidationDetails(
                    isValid = true,
                    status = "Active",
                    generatedM3uUrl = m3uUrl
                 )
            }
        }

        Log.e(TAG, "فشل التحقق من جميع صيغ Xtream لـ $username")
        return@withContext XtreamValidationDetails(isValid = false)
    }
    
    private fun parseSuccessfulXtreamResponse(
        data: JSONObject,
        serverUrl: String,
        user: String,
        pass: String,
        endpoint: String
    ): XtreamValidationDetails {
        val userInfo = data.optJSONObject("user_info") ?: JSONObject()
        val serverInfo = data.optJSONObject("server_info") ?: JSONObject()

        val expDateTimestamp = userInfo.optString("exp_date", null)
        val expDateFormatted = expDateTimestamp?.let {
            try {
                val timestampLong = it.toLong()
                val date = Date(timestampLong * 1000)
                SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(date)
            } catch (e: Exception) { it }
        }

        return XtreamValidationDetails(
            isValid = true,
            endpoint = endpoint,
            realUrl = serverInfo.optString("url", serverUrl),
            status = userInfo.optString("status", "Active"),
            expiryDate = expDateFormatted,
            maxConnections = userInfo.optInt("max_connections", 0),
            activeConnections = userInfo.optInt("active_cons", 0),
            timezone = serverInfo.optString("timezone", ""),
            generatedM3uUrl = "$serverUrl/get.php?username=$user&password=$pass&type=m3u_plus",
            rawResponse = data.toString()
        )
    }

    private suspend fun testUrlAccess(url: String): Boolean {
        return try {
            val request = Request.Builder().url(url).head().build()
            val response = withTimeoutOrNull(5000) { httpClient.newCall(request).execute() }
            response?.isSuccessful ?: false
        } catch (e: Exception) { false }
    }

    fun cleanXtreamUrl(url: String): String {
        var cleanUrl = url.trim().let { if (it.endsWith("/")) it.dropLast(1) else it }
        if (!cleanUrl.startsWith("http://", true) && !cleanUrl.startsWith("https://", true)) {
            cleanUrl = "http://$cleanUrl"
        }
        return cleanUrl
    }

    private fun buildTestUrl(baseUrl: String, format: String, user: String, pass: String): String {
        val path = format.replace("{user}", user).replace("{pass}", pass)
        return "$baseUrl$path"
    }
    
    suspend fun getAccountInfo(playerApiUrl: String): AccountInfo? {
        return try {
            val response = apiService.getUserInfo(playerApiUrl)
            response.userInfo?.toAccountInfo()
        } catch (e: Exception) { null }
    }

    suspend fun getLiveCategories(playerApiUrl: String): List<Category> {
        return try {
            apiService.getLiveCategories("$playerApiUrl&action=get_live_categories").map { it.toCategory() }
        } catch (e: Exception) { emptyList() }
    }
}

interface XtreamApiService {
    @GET
    suspend fun getUserInfo(@Url url: String): XtreamUserInfoResponse

    @GET
    suspend fun getLiveCategories(@Url url: String): List<XtreamCategory>
}

// ... data classes and mappers
fun XtreamUserInfo.toAccountInfo(): AccountInfo {
    val expiryDateFormatted = this.expDate?.let {
        try {
            val timestampLong = it.toLong()
            val date = Date(timestampLong * 1000)
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) { "لا نهائي" }
    }
    return AccountInfo(
        username = this.username,
        status = this.status,
        expiryDate = expiryDateFormatted,
        isTrial = this.isTrial == "1",
        maxConnections = this.maxConnections?.toIntOrNull() ?: 1,
        activeConnections = this.activeCons?.toIntOrNull() ?: 0,
        createdAt = this.createdAt?.let { (it.toLong() * 1000).toString() }
    )
}

fun XtreamCategory.toCategory(): Category {
    return Category(
        id = this.categoryId,
        name = this.categoryName,
        parentId = this.parentId?.toString(),
        type = ContentType.LIVE_TV
    )
}