package com.example.iptvhost.corenetwork.m3u

import com.example.iptvhost.coredata.model.Channel
import com.example.iptvhost.coredata.model.M3uSource
import com.example.iptvhost.coredata.repository.RemoteSourceLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

/**
 * Very lightweight M3U playlist loader. It supports basic #EXTINF records of the form:
 *  #EXTINF:-1 tvg-logo="logo" group-title="group",Channel Name
 *  http://stream-url
 */
class PlaylistLoader(
    private val httpClient: OkHttpClient = OkHttpClient()
) : RemoteSourceLoader<M3uSource> {

    override suspend fun loadChannels(source: M3uSource): List<Channel> = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(source.playlistUrl).build()
        val response = httpClient.newCall(req).execute()
        if (!response.isSuccessful) return@withContext emptyList()

        val body = response.body ?: return@withContext emptyList()
        val reader = BufferedReader(InputStreamReader(body.byteStream()))

        val channels = mutableListOf<Channel>()
        var currentName: String? = null
        var currentLogo: String? = null
        var currentGroup: String? = null

        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("#EXTINF", ignoreCase = true) -> {
                        // Extract name after last comma
                        val commaIdx = trimmed.lastIndexOf(',')
                        if (commaIdx != -1 && commaIdx < trimmed.length - 1) {
                            currentName = trimmed.substring(commaIdx + 1)
                        }
                        // Extract tvg-logo and group-title attrs (simple regex)
                        val logoRegex = "tvg-logo=\"([^\"]+)\"".toRegex()
                        currentLogo = logoRegex.find(trimmed)?.groupValues?.getOrNull(1)
                        val groupRegex = "group-title=\"([^\"]+)\"".toRegex()
                        currentGroup = groupRegex.find(trimmed)?.groupValues?.getOrNull(1)
                    }

                    trimmed.isNotEmpty() && !trimmed.startsWith("#") -> {
                        val url = trimmed
                        val name = currentName ?: url
                        channels.add(
                            Channel(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                streamUrl = url,
                                logoUrl = currentLogo,
                                group = currentGroup
                            )
                        )
                        currentName = null
                        currentLogo = null
                        currentGroup = null
                    }
                }
            }
        }
        channels
    }
}