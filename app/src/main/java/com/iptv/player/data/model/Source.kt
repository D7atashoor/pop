package com.iptv.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class Source(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: SourceType,
    val url: String,
    val username: String? = null,
    val password: String? = null,
    val macAddress: String? = null,
    val portalPath: String? = null,
    val serialNumber: String? = null,
    val deviceId: String? = null,
    val userAgent: String? = null,
    val referer: String? = null,
    val isActive: Boolean = true,
    val lastChecked: Long? = null,
    val accountStatus: String? = null,
    val expiryDate: String? = null,
    val maxConnections: Int? = null,
    val activeConnections: Int? = null,
    val isTrial: Boolean = false,
    val countryCode: String? = null,
    val serverInfo: String? = null, // JSON للمعلومات الإضافية
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class SourceType {
    M3U,           // M3U/M3U8 playlist
    STALKER,       // Stalker Portal
    XTREAM,        // Xtream Codes API
    MAC_PORTAL     // MAC Portal
}

data class M3USource(
    val url: String,
    val userAgent: String? = null,
    val referer: String? = null,
    val epgUrl: String? = null,
    val catchupDays: Int? = null,
    val timeshift: Int? = null
)

data class StalkerSource(
    val portalUrl: String,
    val macAddress: String,
    val portalPath: String = "/stalker_portal/server/load.php",
    val login: String? = null,
    val password: String? = null,
    val serialNumber: String? = null,
    val deviceId: String? = null,
    val stbType: String = "MAG254",
    val token: String? = null
)

data class XtreamSource(
    val serverUrl: String,
    val username: String,
    val password: String,
    val serverProtocol: String = "http",
    val port: String? = null,
    val httpsPort: String? = null
)

data class MacPortalSource(
    val portalUrl: String,
    val macAddress: String,
    val serialNumber: String? = null,
    val deviceId: String? = null,
    val portalPath: String = "/portal.php"
)

data class Channel(
    val id: String,
    val name: String,
    val group: String? = null,
    val logo: String? = null,
    val url: String,
    val epgId: String? = null,
    val channelNumber: String? = null,
    val isHd: Boolean = false,
    val isRadio: Boolean = false,
    val hasCatchup: Boolean = false,
    val catchupDays: Int? = null,
    val catchupSource: String? = null,
    val timeshift: Int? = null,
    val userAgent: String? = null,
    val referer: String? = null,
    val contentType: ContentType = ContentType.LIVE_TV,
    val language: String? = null,
    val country: String? = null,
    val sourceId: Long
)

data class ChannelGroup(
    val name: String,
    val channels: List<Channel>,
    val logo: String? = null,
    val description: String? = null
)

data class Movie(
    val id: String,
    val name: String,
    val description: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    val director: String? = null,
    val cast: String? = null,
    val rating: Double? = null,
    val duration: Int? = null, // in minutes
    val trailer: String? = null,
    val url: String,
    val quality: String? = null,
    val fileSize: Long? = null,
    val language: String? = null,
    val subtitles: List<String>? = null,
    val sourceId: Long
)

data class Series(
    val id: String,
    val name: String,
    val description: String? = null,
    val poster: String? = null,
    val backdrop: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    val director: String? = null,
    val cast: String? = null,
    val rating: Double? = null,
    val trailer: String? = null,
    val totalSeasons: Int? = null,
    val totalEpisodes: Int? = null,
    val status: String? = null, // ongoing, completed, etc.
    val language: String? = null,
    val episodes: List<Episode>,
    val sourceId: Long
)

data class Episode(
    val id: String,
    val name: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val description: String? = null,
    val poster: String? = null,
    val url: String,
    val duration: Int? = null, // in minutes
    val airDate: String? = null,
    val rating: Double? = null,
    val quality: String? = null
)

enum class ContentType {
    LIVE_TV,
    VOD,
    SERIES,
    RADIO,
    UNKNOWN
}

data class EpgProgram(
    val id: String,
    val channelId: String,
    val title: String,
    val description: String? = null,
    val startTime: Long, // timestamp
    val endTime: Long,   // timestamp
    val category: String? = null,
    val rating: String? = null,
    val language: String? = null,
    val isLive: Boolean = false,
    val hasRecording: Boolean = false
)

data class EpgChannel(
    val id: String,
    val name: String,
    val logo: String? = null,
    val programs: List<EpgProgram>
)

data class ServerInfo(
    val host: String,
    val port: Int? = null,
    val protocol: String = "http",
    val country: String? = null,
    val countryCode: String? = null,
    val city: String? = null,
    val isp: String? = null,
    val timezone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val lastPing: Long? = null,
    val responseTime: Long? = null,
    val isOnline: Boolean = true
)

data class AccountInfo(
    val username: String? = null,
    val status: String? = null,
    val expiryDate: String? = null,
    val isActive: Boolean = true,
    val isTrial: Boolean = false,
    val maxConnections: Int? = null,
    val activeConnections: Int? = null,
    val createdAt: String? = null,
    val lastLogin: String? = null,
    val allowedOutputFormats: List<String>? = null,
    val walletBalance: Double? = null
)

data class SourceStatistics(
    val sourceId: Long,
    val totalChannels: Int = 0,
    val liveChannels: Int = 0,
    val vodChannels: Int = 0,
    val seriesChannels: Int = 0,
    val radioChannels: Int = 0,
    val hdChannels: Int = 0,
    val channelsWithEpg: Int = 0,
    val channelsWithLogo: Int = 0,
    val totalCategories: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class Category(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val type: ContentType,
    val count: Int = 0,
    val logo: String? = null,
    val description: String? = null
)

data class SourceValidationResult(
    val isValid: Boolean,
    val sourceType: SourceType?,
    val issues: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val statistics: SourceStatistics? = null,
    val serverInfo: ServerInfo? = null,
    val accountInfo: AccountInfo? = null
)

data class SearchFilter(
    val query: String? = null,
    val contentType: ContentType? = null,
    val category: String? = null,
    val language: String? = null,
    val country: String? = null,
    val minRating: Double? = null,
    val year: Int? = null,
    val hasLogo: Boolean? = null,
    val hasEpg: Boolean? = null,
    val isHd: Boolean? = null
)

data class SearchResult<T>(
    val results: List<T>,
    val totalCount: Int,
    val query: String,
    val suggestions: List<String> = emptyList()
)

data class Favorite(
    val id: Long = 0,
    val itemId: String,
    val itemType: ContentType,
    val sourceId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

data class WatchHistory(
    val id: Long = 0,
    val itemId: String,
    val itemType: ContentType,
    val sourceId: Long,
    val watchedAt: Long = System.currentTimeMillis(),
    val watchDuration: Long = 0, // in seconds
    val lastPosition: Long = 0    // in seconds
)

data class StreamingOptions(
    val quality: String? = null,
    val bitrate: Int? = null,
    val resolution: String? = null,
    val codec: String? = null,
    val fps: Int? = null,
    val audioCodec: String? = null,
    val audioChannels: String? = null,
    val subtitles: List<Subtitle> = emptyList()
)

data class Subtitle(
    val language: String,
    val url: String,
    val format: String = "srt" // srt, vtt, ass, etc.
)