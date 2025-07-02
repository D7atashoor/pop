package com.example.iptvhost.corenetwork.stalker

import com.example.iptvhost.coredata.model.Channel
import com.example.iptvhost.coredata.model.StalkerSource
import com.example.iptvhost.coredata.repository.RemoteSourceLoader

/**
 * A full Stalker/MAG portal implementation is complex (token negotiation, cookies, stbEmu headers…).
 * This class contains just a stub so that the project compiles. You can implement the real logic later.
 */
class StalkerClient : RemoteSourceLoader<StalkerSource> {
    override suspend fun loadChannels(source: StalkerSource): List<Channel> {
        // TODO: Implement real Stalker portal communication.
        return emptyList()
    }
}