package com.example.iptvhost.corenetwork.xtream

import com.example.iptvhost.coredata.model.Channel
import com.example.iptvhost.coredata.model.XtreamSource
import com.example.iptvhost.coredata.repository.RemoteSourceLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.UUID

/**
 * Minimal Xtream Codes client reading the `player_api.php` endpoint.
 * NOTE: Implementation is intentionally lightweight and may not handle every edge-case.
 */
class XtreamCodesClient(
    private val httpClient: OkHttpClient = OkHttpClient()
) : RemoteSourceLoader<XtreamSource> {

    override suspend fun loadChannels(source: XtreamSource): List<Channel> = withContext(Dispatchers.IO) {
        val base = source.baseUrl.trimEnd('/')
        val url = ("$base/player_api.php").toHttpUrlOrNull()?.newBuilder()
            .addQueryParameter("username", source.username)
            .addQueryParameter("password", source.password)
            .addQueryParameter("action", "get_live_streams")
            ?.build() ?: return@withContext emptyList()

        val req = Request.Builder().url(url).build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful) return@withContext emptyList()

        val jsonStr = resp.body?.string().orEmpty()
        if (jsonStr.isBlank()) return@withContext emptyList()

        val arr = JSONArray(jsonStr)
        val result = mutableListOf<Channel>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val name = obj.optString("name")
            val streamId = obj.optString("stream_id")
            val logo = obj.optString("stream_icon")
            val category = obj.optString("category_name")
            val streamUrl = "$base/live/${source.username}/${source.password}/$streamId.ts"
            result.add(
                Channel(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    streamUrl = streamUrl,
                    logoUrl = logo.ifBlank { null },
                    group = category.ifBlank { null }
                )
            )
        }
        result
    }
}