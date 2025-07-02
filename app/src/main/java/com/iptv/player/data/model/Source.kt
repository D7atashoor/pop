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
    val isActive: Boolean = true,
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
    val referer: String? = null
)

data class StalkerSource(
    val portalUrl: String,
    val macAddress: String,
    val login: String? = null,
    val password: String? = null
)

data class XtreamSource(
    val serverUrl: String,
    val username: String,
    val password: String
)

data class MacPortalSource(
    val portalUrl: String,
    val macAddress: String,
    val serialNumber: String? = null
)

data class Channel(
    val id: String,
    val name: String,
    val group: String? = null,
    val logo: String? = null,
    val url: String,
    val epgId: String? = null,
    val sourceId: Long
)

data class ChannelGroup(
    val name: String,
    val channels: List<Channel>
)

data class Movie(
    val id: String,
    val name: String,
    val description: String? = null,
    val poster: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    val url: String,
    val sourceId: Long
)

data class Series(
    val id: String,
    val name: String,
    val description: String? = null,
    val poster: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    val episodes: List<Episode>,
    val sourceId: Long
)

data class Episode(
    val id: String,
    val name: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val url: String,
    val poster: String? = null
)