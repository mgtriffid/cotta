package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.impl.EntitiesImpl

interface Entities {
    companion object {
        fun getInstance(): Entities = EntitiesImpl()
    }

    fun createEntity(): Entity
    fun get(id: EntityId): Entity
    fun all(): Collection<Entity>
    fun createEntity(id: EntityId): Entity
    fun remove(id: EntityId)
}
