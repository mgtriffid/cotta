package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.impl.EntitiesImpl

interface Entities {
    companion object {
        fun getInstance(): Entities = EntitiesImpl()
    }

    fun createEntity(ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity
    fun get(id: EntityId): Entity
    fun all(): Collection<Entity>
    // GROOM ISP violated here, clearly
    fun createEntity(id: EntityId, ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity
    fun remove(id: EntityId)
}
