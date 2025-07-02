package com.iptv.player.data.network

import com.iptv.player.data.model.Channel
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3UParser @Inject constructor() {
    
    companion object {
        private const val TAG = "M3UParser"
        private const val EXTINF_TAG = "#EXTINF:"
        private const val EXTM3U_TAG = "#EXTM3U"
        private const val EXT_X_VERSION_TAG = "#EXT-X-VERSION"
        private const val EXT_X_STREAM_INF_TAG = "#EXT-X-STREAM-INF"
        private const val KODIPROP_TAG = "#KODIPROP:"
        private const val EXTVLCOPT_TAG = "#EXTVLCOPT:"
        
        // أنماط RegEx محسنة لاستخراج المعلومات
        private val TVG_ID_PATTERN = Pattern.compile("tvg-id=\"([^\"]*)\"|tvg-id='([^']*)'|tvg-id=([^\\s,]*)")
        private val TVG_NAME_PATTERN = Pattern.compile("tvg-name=\"([^\"]*)\"|tvg-name='([^']*)'|tvg-name=([^\\s,]*)")
        private val TVG_LOGO_PATTERN = Pattern.compile("tvg-logo=\"([^\"]*)\"|tvg-logo='([^']*)'|tvg-logo=([^\\s,]*)")
        private val TVG_CHNO_PATTERN = Pattern.compile("tvg-chno=\"([^\"]*)\"|tvg-chno='([^']*)'|tvg-chno=([^\\s,]*)")
        private val TVG_SHIFT_PATTERN = Pattern.compile("tvg-shift=\"([^\"]*)\"|tvg-shift='([^']*)'|tvg-shift=([^\\s,]*)")
        private val GROUP_TITLE_PATTERN = Pattern.compile("group-title=\"([^\"]*)\"|group-title='([^']*)'|group-title=([^\\s,]*)")
        private val RADIO_PATTERN = Pattern.compile("radio=\"([^\"]*)\"|radio='([^']*)'|radio=([^\\s,]*)")
        private val CATCHUP_PATTERN = Pattern.compile("catchup=\"([^\"]*)\"|catchup='([^']*)'|catchup=([^\\s,]*)")
        private val CATCHUP_DAYS_PATTERN = Pattern.compile("catchup-days=\"([^\"]*)\"|catchup-days='([^']*)'|catchup-days=([^\\s,]*)")
        private val CATCHUP_SOURCE_PATTERN = Pattern.compile("catchup-source=\"([^\"]*)\"|catchup-source='([^']*)'|catchup-source=([^\\s,]*)")
        private val TIMESHIFT_PATTERN = Pattern.compile("timeshift=\"([^\"]*)\"|timeshift='([^']*)'|timeshift=([^\\s,]*)")
        private val USER_AGENT_PATTERN = Pattern.compile("user-agent=\"([^\"]*)\"|user-agent='([^']*)'|user-agent=([^\\s,]*)")
        private val REFERER_PATTERN = Pattern.compile("referer=\"([^\"]*)\"|referer='([^']*)'|referer=([^\\s,]*)")
        
        // أنماط للكشف عن أنواع المحتوى
        private val VOD_PATTERNS = listOf("movie", "film", "cinema", "vod", ".mp4", ".mkv", ".avi")
        private val SERIES_PATTERNS = listOf("series", "episode", "season", "tv show")
        private val RADIO_PATTERNS = listOf("radio", "music", "fm", "am")
    }
    
    /**
     * تحليل محتوى M3U من نص
     */
    suspend fun parseFromContent(content: String, sourceId: Long): ParseResult {
        return try {
            Log.d(TAG, "بدء تحليل M3U من المحتوى")
            
            val lines = content.lines()
            val channels = mutableListOf<Channel>()
            val statistics = M3UStatistics()
            
            var currentChannel: M3UChannelInfo? = null
            var lineNumber = 0
            
            for (line in lines) {
                lineNumber++
                val trimmedLine = line.trim()
                
                when {
                    trimmedLine.startsWith(EXTM3U_TAG) -> {
                        statistics.hasValidHeader = true
                        parseM3UHeader(trimmedLine, statistics)
                    }
                    
                    trimmedLine.startsWith(EXTINF_TAG) -> {
                        currentChannel = parseExtInfLine(trimmedLine)
                        statistics.totalExtInfLines++
                    }
                    
                    trimmedLine.startsWith(KODIPROP_TAG) -> {
                        currentChannel?.kodiProps?.add(trimmedLine.substring(KODIPROP_TAG.length))
                    }
                    
                    trimmedLine.startsWith(EXTVLCOPT_TAG) -> {
                        currentChannel?.vlcOpts?.add(trimmedLine.substring(EXTVLCOPT_TAG.length))
                    }
                    
                    isValidStreamUrl(trimmedLine) -> {
                        currentChannel?.let { channelInfo ->
                            val channel = createChannelFromInfo(channelInfo, trimmedLine, sourceId)
                            channels.add(channel)
                            updateStatistics(channel, statistics)
                        }
                        currentChannel = null
                    }
                    
                    trimmedLine.startsWith("#") -> {
                        // تجاهل التعليقات الأخرى
                        statistics.totalCommentLines++
                    }
                    
                    trimmedLine.isNotEmpty() -> {
                        // سطر غير معروف
                        statistics.totalUnknownLines++
                    }
                }
            }
            
            statistics.totalChannels = channels.size
            statistics.totalLines = lineNumber
            
            Log.d(TAG, "تم تحليل M3U بنجاح: ${channels.size} قناة")
            ParseResult.Success(channels, statistics)
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في تحليل M3U من المحتوى", e)
            ParseResult.Error("خطأ في تحليل M3U: ${e.localizedMessage ?: e.message}")
        }
    }
    
    /**
     * تحليل M3U من URL - مع معالجة محسنة للأخطاء
     */
    suspend fun parseFromUrl(url: String, sourceId: Long): ParseResult {
        return try {
            Log.d(TAG, "بدء تحليل M3U من URL: $url")
            
            val content = downloadM3UContent(url)
            if (content.isBlank()) {
                Log.e(TAG, "المحتوى فارغ من URL: $url")
                return ParseResult.Error("المحتوى فارغ أو لا يمكن الوصول إليه")
            }
            
            Log.d(TAG, "تم تحميل المحتوى بنجاح، البدء في التحليل...")
            parseFromContent(content, sourceId)
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في تحليل M3U من URL: $url", e)
            ParseResult.Error("خطأ في تحميل M3U من URL: ${e.localizedMessage ?: e.message}")
        }
    }
    
    /**
     * تحليل سطر EXTINF
     */
    private fun parseExtInfLine(line: String): M3UChannelInfo {
        val channelInfo = M3UChannelInfo()
        
        // استخراج المدة والاسم
        val parts = line.substring(EXTINF_TAG.length).split(",", limit = 2)
        if (parts.size >= 2) {
            channelInfo.duration = parts[0].trim().toIntOrNull() ?: -1
            channelInfo.name = parts[1].trim()
        }
        
        // استخراج الخصائص المختلفة
        channelInfo.tvgId = extractPattern(line, TVG_ID_PATTERN)
        channelInfo.tvgName = extractPattern(line, TVG_NAME_PATTERN)
        channelInfo.tvgLogo = extractPattern(line, TVG_LOGO_PATTERN)
        channelInfo.tvgChno = extractPattern(line, TVG_CHNO_PATTERN)
        channelInfo.tvgShift = extractPattern(line, TVG_SHIFT_PATTERN)?.toIntOrNull()
        channelInfo.groupTitle = extractPattern(line, GROUP_TITLE_PATTERN)
        channelInfo.isRadio = extractPattern(line, RADIO_PATTERN) == "true"
        channelInfo.catchup = extractPattern(line, CATCHUP_PATTERN)
        channelInfo.catchupDays = extractPattern(line, CATCHUP_DAYS_PATTERN)?.toIntOrNull()
        channelInfo.catchupSource = extractPattern(line, CATCHUP_SOURCE_PATTERN)
        channelInfo.timeshift = extractPattern(line, TIMESHIFT_PATTERN)?.toIntOrNull()
        channelInfo.userAgent = extractPattern(line, USER_AGENT_PATTERN)
        channelInfo.referer = extractPattern(line, REFERER_PATTERN)
        
        return channelInfo
    }
    
    /**
     * استخراج قيمة بناءً على النمط
     */
    private fun extractPattern(text: String, pattern: Pattern): String? {
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            // جرب المجموعات الثلاث (مزدوجة، مفردة، بدون اقتباس)
            matcher.group(1) ?: matcher.group(2) ?: matcher.group(3)
        } else null
    }
    
    /**
     * تحليل header الخاص بـ M3U
     */
    private fun parseM3UHeader(line: String, statistics: M3UStatistics) {
        // يمكن إضافة تحليل معلومات إضافية من header
        if (line.contains("url-tvg") || line.contains("x-tvg-url")) {
            statistics.hasEpgInfo = true
        }
    }
    
    /**
     * إنشاء Channel من معلومات M3U
     */
    private fun createChannelFromInfo(
        channelInfo: M3UChannelInfo, 
        url: String, 
        sourceId: Long
    ): Channel {
        return Channel(
            id = generateChannelId(channelInfo, url),
            name = cleanChannelName(channelInfo.name ?: "Unknown Channel"),
            group = channelInfo.groupTitle?.takeIf { it.isNotEmpty() },
            logo = channelInfo.tvgLogo?.takeIf { it.isNotEmpty() },
            url = url,
            epgId = channelInfo.tvgId?.takeIf { it.isNotEmpty() },
            sourceId = sourceId
        )
    }
    
    /**
     * توليد معرف فريد للقناة
     */
    private fun generateChannelId(channelInfo: M3UChannelInfo, url: String): String {
        return channelInfo.tvgId?.takeIf { it.isNotEmpty() }
            ?: channelInfo.tvgName?.takeIf { it.isNotEmpty() }
            ?: channelInfo.name?.takeIf { it.isNotEmpty() }
            ?: url.hashCode().toString()
    }
    
    /**
     * تنظيف اسم القناة
     */
    private fun cleanChannelName(name: String): String {
        return name.replace(Regex("[^\\w\\s\\-\\[\\]()]+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * التحقق من صحة رابط البث
     */
    private fun isValidStreamUrl(url: String): Boolean {
        return url.startsWith("http://", ignoreCase = true) || 
               url.startsWith("https://", ignoreCase = true) || 
               url.startsWith("udp://", ignoreCase = true) || 
               url.startsWith("rtp://", ignoreCase = true) || 
               url.startsWith("rtsp://", ignoreCase = true) ||
               url.startsWith("rtmp://", ignoreCase = true) ||
               url.startsWith("file://", ignoreCase = true)
    }
    
    /**
     * تحديث الإحصائيات
     */
    private fun updateStatistics(channel: Channel, statistics: M3UStatistics) {
        // تحديث عدد القنوات حسب النوع
        when (detectContentType(channel.name, channel.url)) {
            ContentType.LIVE_TV -> statistics.liveChannels++
            ContentType.VOD -> statistics.vodChannels++
            ContentType.SERIES -> statistics.seriesChannels++
            ContentType.RADIO -> statistics.radioChannels++
            ContentType.UNKNOWN -> statistics.unknownChannels++
        }
        
        // تحديث الفئات
        channel.group?.let { group ->
            statistics.categories[group] = statistics.categories.getOrDefault(group, 0) + 1
        }
        
        // تحديث معلومات EPG
        if (channel.epgId != null) {
            statistics.channelsWithEpg++
        }
        
        // تحديث معلومات الشعارات
        if (channel.logo != null) {
            statistics.channelsWithLogo++
        }
    }
    
    /**
     * كشف نوع المحتوى
     */
    private fun detectContentType(name: String, url: String): ContentType {
        val lowerName = name.lowercase()
        val lowerUrl = url.lowercase()
        
        return when {
            VOD_PATTERNS.any { lowerName.contains(it) || lowerUrl.contains(it) } -> ContentType.VOD
            SERIES_PATTERNS.any { lowerName.contains(it) || lowerUrl.contains(it) } -> ContentType.SERIES
            RADIO_PATTERNS.any { lowerName.contains(it) || lowerUrl.contains(it) } -> ContentType.RADIO
            url.contains("/live/") || url.contains("m3u8") -> ContentType.LIVE_TV
            else -> ContentType.UNKNOWN
        }
    }
    
    /**
     * تحميل محتوى M3U من URL - مع معالجة أفضل للأخطاء
     */
    private suspend fun downloadM3UContent(url: String): String {
        return try {
            Log.d(TAG, "تحميل M3U من: $url")
            
            val connection = URL(url).openConnection()
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.setRequestProperty("User-Agent", "IPTV Player/1.0")
            connection.setRequestProperty("Accept", "*/*")
            
            BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
                val content = reader.readText()
                Log.d(TAG, "تم تحميل ${content.length} حرف من المحتوى")
                
                // تحقق أساسي من المحتوى
                if (content.contains("#EXTM3U") || content.contains("http")) {
                    content
                } else {
                    Log.w(TAG, "المحتوى لا يبدو كملف M3U صالح")
                    content // أعد المحتوى على أي حال للمحاولة
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في تحميل M3U", e)
            throw Exception("فشل في تحميل M3U: ${e.localizedMessage ?: e.message}")
        }
    }
    
    /**
     * تحليل أو تصنيف قائمة الروابط
     */
    fun categorizeChannels(channels: List<Channel>): Map<String, List<Channel>> {
        return channels.groupBy { channel ->
            channel.group?.takeIf { it.isNotEmpty() }
                ?: categorizeChannelByName(channel.name)
        }
    }
    
    /**
     * تصنيف القناة حسب الاسم
     */
    private fun categorizeChannelByName(channelName: String): String {
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
            
            name.contains("radio") || name.contains("fm") || name.contains("am") -> "Radio"
            
            else -> "General"
        }
    }
    
    /**
     * استخراج معلومات EPG من M3U
     */
    fun extractEpgUrls(content: String): List<String> {
        val epgUrls = mutableListOf<String>()
        val lines = content.lines()
        
        for (line in lines) {
            if (line.startsWith(EXTM3U_TAG)) {
                // البحث عن url-tvg أو x-tvg-url
                val tvgPattern = Pattern.compile("url-tvg=\"([^\"]*)\"|x-tvg-url=\"([^\"]*)\"|tvg-url=\"([^\"]*)\"")
                val matcher = tvgPattern.matcher(line)
                while (matcher.find()) {
                    val epgUrl = matcher.group(1) ?: matcher.group(2) ?: matcher.group(3)
                    if (epgUrl?.isNotEmpty() == true) {
                        epgUrls.add(epgUrl)
                    }
                }
            }
        }
        
        return epgUrls.distinct()
    }
    
    /**
     * التحقق من صحة ملف M3U
     */
    fun validateM3U(content: String): ValidationResult {
        val issues = mutableListOf<String>()
        val lines = content.lines()
        
        // التحقق من وجود header
        if (!lines.any { it.startsWith(EXTM3U_TAG) }) {
            issues.add("لا يحتوي على header صحيح (#EXTM3U)")
        }
        
        // التحقق من وجود قنوات
        val channelCount = lines.count { isValidStreamUrl(it) }
        if (channelCount == 0) {
            issues.add("لا يحتوي على أي قنوات صالحة")
        }
        
        // التحقق من تطابق EXTINF مع URLs
        val extinfCount = lines.count { it.startsWith(EXTINF_TAG) }
        if (extinfCount != channelCount && channelCount > 0) {
            issues.add("عدم تطابق بين سطور EXTINF ($extinfCount) وروابط القنوات ($channelCount)")
        }
        
        return ValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
            channelCount = channelCount,
            hasEpgInfo = lines.any { it.contains("url-tvg") || it.contains("x-tvg-url") }
        )
    }
}

// Data Classes for M3U parsing
data class M3UChannelInfo(
    var name: String? = null,
    var duration: Int = -1,
    var tvgId: String? = null,
    var tvgName: String? = null,
    var tvgLogo: String? = null,
    var tvgChno: String? = null,
    var tvgShift: Int? = null,
    var groupTitle: String? = null,
    var isRadio: Boolean = false,
    var catchup: String? = null,
    var catchupDays: Int? = null,
    var catchupSource: String? = null,
    var timeshift: Int? = null,
    var userAgent: String? = null,
    var referer: String? = null,
    val kodiProps: MutableList<String> = mutableListOf(),
    val vlcOpts: MutableList<String> = mutableListOf()
)

data class M3UStatistics(
    var totalLines: Int = 0,
    var totalChannels: Int = 0,
    var totalExtInfLines: Int = 0,
    var totalCommentLines: Int = 0,
    var totalUnknownLines: Int = 0,
    var hasValidHeader: Boolean = false,
    var hasEpgInfo: Boolean = false,
    var liveChannels: Int = 0,
    var vodChannels: Int = 0,
    var seriesChannels: Int = 0,
    var radioChannels: Int = 0,
    var unknownChannels: Int = 0,
    var channelsWithEpg: Int = 0,
    var channelsWithLogo: Int = 0,
    val categories: MutableMap<String, Int> = mutableMapOf()
)

enum class ContentType {
    LIVE_TV, VOD, SERIES, RADIO, UNKNOWN
}

sealed class ParseResult {
    data class Success(
        val channels: List<Channel>,
        val statistics: M3UStatistics
    ) : ParseResult()
    
    data class Error(val message: String) : ParseResult()
}

data class ValidationResult(
    val isValid: Boolean,
    val issues: List<String>,
    val channelCount: Int,
    val hasEpgInfo: Boolean
)