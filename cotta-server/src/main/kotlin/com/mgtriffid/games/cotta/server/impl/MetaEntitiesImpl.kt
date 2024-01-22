package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.server.MetaEntities

class MetaEntitiesImpl : MetaEntities {
    private val data = HashMap<PlayerId, EntityId>()
    override fun get(playerId: PlayerId): EntityId = data[playerId]
        ?: throw IllegalStateException("No meta entity for player ${playerId.id}")

    override fun set(playerId: PlayerId, entityId: EntityId) {
        data[playerId] = entityId
    }
}
