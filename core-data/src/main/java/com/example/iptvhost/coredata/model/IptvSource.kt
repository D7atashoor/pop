package com.example.iptvhost.coredata.model

/**
 * Represents a generic IPTV source definition provided by the end-user.
 * The library itself لا يضم محتوى بل يسمح فقط بتخزين معلومات الاتصال.
 */
sealed interface IptvSource {
    /** Locally-generated identifier (e.g. UUID) */
    val id: String

    /** Friendly name chosen by the user (e.g. "Home M3U", "Office Xtream") */
    val name: String
}

/**
 * Simple HTTP/HTTPS link (or local file URI) pointing to a raw .m3u/.m3u8 playlist.
 */
data class M3uSource(
    override val id: String,
    override val name: String,
    val playlistUrl: String
) : IptvSource

/**
 * Xtream Codes / XUI-One servers require base URL + credentials.
 */
data class XtreamSource(
    override val id: String,
    override val name: String,
    val baseUrl: String, // e.g. http://example.com:8080
    val username: String,
    val password: String
) : IptvSource

/**
 * Stalker portal (MAG) usually needs portal URL and device-like MAC address.
 */
data class StalkerSource(
    override val id: String,
    override val name: String,
    val portalUrl: String,
    val macAddress: String
) : IptvSource

/**
 * MAC Portal – often similar to Stalker but accessed only via MAC auth.
 */
data class MacPortalSource(
    override val id: String,
    override val name: String,
    val portalUrl: String,
    val macAddress: String
) : IptvSource