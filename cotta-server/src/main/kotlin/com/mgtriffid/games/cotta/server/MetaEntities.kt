package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface MetaEntities {
    operator fun get(playerId: PlayerId): EntityId
    operator fun set(playerId: PlayerId, entityId: EntityId)
}
