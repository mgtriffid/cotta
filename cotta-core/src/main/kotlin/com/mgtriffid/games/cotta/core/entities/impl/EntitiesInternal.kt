package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface EntitiesInternal : Entities {
    fun currentId(): Int
    fun create(id: EntityId, ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity
    fun setIdGenerator(idSequence: Int)
}
