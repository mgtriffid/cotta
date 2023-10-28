package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.PlayerId

interface MetaEntities {
    operator fun get(playerId: PlayerId): EntityId
    operator fun set(playerId: PlayerId, entityId: EntityId)
}
