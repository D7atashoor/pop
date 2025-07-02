package com.example.iptvhost.coredata.model

/**
 * Minimal representation of a channel or VOD item.
 */
data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val group: String? = null
)