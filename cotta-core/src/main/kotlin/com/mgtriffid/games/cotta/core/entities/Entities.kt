package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesImpl

interface Entities {
    companion object {
        fun getInstance(): Entities = EntitiesImpl()
    }

    fun create(ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity
    fun get(id: EntityId): Entity
    fun all(): Collection<Entity>
    fun dynamic(): Collection<Entity>
    // GROOM ISP violated here, clearly
    fun create(id: EntityId, ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity
    fun remove(id: EntityId)
    fun createStatic(id: EntityId): Entity
}
