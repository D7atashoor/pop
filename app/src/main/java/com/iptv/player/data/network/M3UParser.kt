package com.iptv.player.data.network

import com.iptv.player.data.model.Channel
import java.io.BufferedReader
import java.io.StringReader
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3UParser @Inject constructor() {
    
    private val extinf = Pattern.compile("#EXTINF:(-?\\d+)(.*)?,(.*)").toRegex()
    private val tvgName = Pattern.compile("tvg-name=\"([^\"]*)?\"").toRegex()
    private val tvgLogo = Pattern.compile("tvg-logo=\"([^\"]*)?\"").toRegex()
    private val tvgId = Pattern.compile("tvg-id=\"([^\"]*)?\"").toRegex()
    private val groupTitle = Pattern.compile("group-title=\"([^\"]*)?\"").toRegex()
    
    suspend fun parseM3U(content: String, sourceId: Long): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(content))
        
        var line: String?
        var currentChannel: Channel? = null
        
        while (reader.readLine().also { line = it } != null) {
            line?.let { currentLine ->
                when {
                    currentLine.startsWith("#EXTINF:") -> {
                        currentChannel = parseExtinf(currentLine, sourceId)
                    }
                    currentLine.startsWith("http") && currentChannel != null -> {
                        currentChannel?.let { channel ->
                            channels.add(channel.copy(url = currentLine.trim()))
                        }
                        currentChannel = null
                    }
                }
            }
        }
        
        return channels
    }
    
    private fun parseExtinf(line: String, sourceId: Long): Channel? {
        val match = extinf.find(line) ?: return null
        
        val duration = match.groupValues[1]
        val attributes = match.groupValues[2]
        val title = match.groupValues[3].trim()
        
        val tvgNameMatch = tvgName.find(attributes)
        val tvgLogoMatch = tvgLogo.find(attributes)
        val tvgIdMatch = tvgId.find(attributes)
        val groupTitleMatch = groupTitle.find(attributes)
        
        return Channel(
            id = tvgIdMatch?.groupValues?.get(1) ?: title.hashCode().toString(),
            name = tvgNameMatch?.groupValues?.get(1) ?: title,
            group = groupTitleMatch?.groupValues?.get(1),
            logo = tvgLogoMatch?.groupValues?.get(1),
            url = "", // Will be set when URL line is found
            epgId = tvgIdMatch?.groupValues?.get(1),
            sourceId = sourceId
        )
    }
}