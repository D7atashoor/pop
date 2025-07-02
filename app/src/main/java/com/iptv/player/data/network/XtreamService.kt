import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamService @Inject constructor(
    private val apiService: XtreamApiService
) {
    
    companion object {
        private const val TAG = "XtreamService"
        
        // صيغ M3U متعددة للتحقق
        val M3U_URL_FORMATS = listOf(
            "/get.php?username={user}&password={pass}&type=m3u_plus&output=ts",
            "/get.php?username={user}&password={pass}&type=m3u&output=ts",
            "/get.php?username={user}&password={pass}&type=m3u_plus",
            "/live/{user}/{pass}/",
            "/{user}/{pass}/all.m3u",
            "/{user}/{pass}/live.m3u",
            "/m3u/{user}/{pass}/",
            "/iptv/{user}/{pass}/",
            "/{user}/{pass}/tv.m3u",
            "/get.php?username={user}&password={pass}&output=m3u8",
            "/enigma2.php?username={user}&password={pass}&type=get_vod_categories"
        )
    }
    
    private var username: String? = null
    private var password: String? = null
    
    /**
     * التحقق من صحة مصدر Xtream Codes مع تجربة صيغ متعددة
     */
    suspend fun validateXtreamSource(
        url: String,
        username: String,
        password: String
    ): Pair<Boolean, String?> {
        val cleanUrl = cleanXtreamUrl(url)
        
        Log.d(TAG, "بدء التحقق من Xtream Codes: $cleanUrl")
        
        for (format in M3U_URL_FORMATS) {
            val testUrl = buildTestUrl(cleanUrl, format, username, password)
            Log.d(TAG, "تجربة الرابط: $testUrl")
            
            try {
                val request = Request.Builder()
                    .url(testUrl)
                    .head() // استخدام HEAD للسرعة
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .addHeader("Accept", "*/*")
                    .addHeader("Connection", "keep-alive")
                    .build()
                
                val response = withTimeoutOrNull(5000) {
                    httpClient.newCall(request).execute()
                }
                
                response?.use {
                    if (it.isSuccessful) {
                        Log.d(TAG, "تم العثور على رابط M3U صالح: $testUrl")
                        return true to testUrl // إرجاع الرابط الناجح
                    }
                }
                
            } catch (e: Exception) {
                Log.d(TAG, "فشل اختبار $testUrl: ${e.message}")
                continue
            }
        }
        
        // إذا فشلت كل صيغ M3U، جرب player_api.php
        try {
            val playerApiUrl = "$cleanUrl/player_api.php?username=$username&password=$password"
            val info = apiService.getUserInfo(playerApiUrl)
            
            if (info.userInfo?.status?.equals("Active", ignoreCase = true) == true) {
                Log.d(TAG, "تم التحقق من player_api.php بنجاح")
                return true to "$cleanUrl/player_api.php"
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل التحقق من player_api.php", e)
        }
        
        Log.e(TAG, "فشل التحقق من جميع صيغ Xtream")
        return false to null
    }
    
    /**
     * تنظيف رابط Xtream
     */
    private fun cleanXtreamUrl(url: String): String {
        var cleanUrl = url.trim().let { if (it.endsWith("/")) it.dropLast(1) else it }
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "http://$cleanUrl"
        }
        return cleanUrl
    }
    
    /**
     * بناء رابط اختبار
     */
    private fun buildTestUrl(baseUrl: String, format: String, user: String, pass: String): String {
        val path = format.replace("{user}", user).replace("{pass}", pass)
        return "$baseUrl$path"
    }
    
    // ... existing code ...
}

interface XtreamApiService {
    
    @GET
    suspend fun getUserInfo(
        @Url url: String
    ): XtreamUserInfoResponse
    
    // ... existing code ...
}

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .addInterceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .header("Accept", "*/*")
            .header("Connection", "keep-alive")
            .build()
        chain.proceed(newRequest)
    }
    .build()

private val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("http://localhost/") // سيتم استبدالها بـ @Url
    .client(httpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

fun provideXtreamApiService(): XtreamApiService {
    return retrofit.create(XtreamApiService::class.java)
}

// ... existing code ...