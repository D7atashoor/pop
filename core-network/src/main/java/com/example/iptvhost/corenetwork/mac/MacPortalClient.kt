package com.example.iptvhost.corenetwork.mac

import com.example.iptvhost.coredata.model.Channel
import com.example.iptvhost.coredata.model.MacPortalSource
import com.example.iptvhost.coredata.repository.RemoteSourceLoader

/**
 * MAC-only portals are often similar to Stalker. Implementation deferred.
 */
class MacPortalClient : RemoteSourceLoader<MacPortalSource> {
    override suspend fun loadChannels(source: MacPortalSource): List<Channel> {
        // TODO: Implement real MAC portal parsing.
        return emptyList()
    }
}