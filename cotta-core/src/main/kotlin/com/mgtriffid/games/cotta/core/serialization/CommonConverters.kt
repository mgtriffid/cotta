package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto
import com.mgtriffid.games.cotta.core.serialization.dto.EntityOwnedByDto

fun EntityIdDto.toEntityId(): EntityId {
    return EntityId(id)
}

fun Entity.OwnedBy.toDto(): EntityOwnedByDto {
    val ret = EntityOwnedByDto()
    when (this) {
        is Entity.OwnedBy.Player -> {
            ret.ownedBySystem = false
            ret.playerId = playerId.id
        }

        is Entity.OwnedBy.System -> {
            ret.ownedBySystem = true
            ret.playerId = 0
        }
    }
    return ret
}

fun EntityOwnedByDto.toOwnedBy(): Entity.OwnedBy {
    return if (ownedBySystem) {
        Entity.OwnedBy.System
    } else {
        Entity.OwnedBy.Player(PlayerId(playerId))
    }
}
