package com.example.iptvhost.coredata.repository

import com.example.iptvhost.coredata.model.Channel
import com.example.iptvhost.coredata.model.IptvSource
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over local persistence for the user's sources (e.g. DataStore / Room).
 */
interface SourceRepository {
    fun observeSources(): Flow<List<IptvSource>>
    suspend fun getSources(): List<IptvSource>
    suspend fun addSource(source: IptvSource)
    suspend fun removeSource(sourceId: String)
}

/**
 * Generic interface to fetch remote data (channels, categories, EPG) for a given source.
 * Implementations are expected to live in the `core-network` module.
 */
interface RemoteSourceLoader<S : IptvSource> {
    suspend fun loadChannels(source: S): List<Channel>
}