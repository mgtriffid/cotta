package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesImpl
import com.mgtriffid.games.cotta.core.exceptions.EntityNotExistsException

interface Entities {
    fun create(ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity
    fun get(id: EntityId): Entity?
    fun currentId(): Int
    fun getOrNotFound(id: EntityId): Entity = get(id) ?: throw EntityNotExistsException("Could not find entity $id")
    fun all(): Collection<Entity>
    fun dynamic(): Collection<Entity>
    // GROOM ISP violated here, clearly
    fun create(id: EntityId, ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity
    fun remove(id: EntityId)
    fun createStatic(id: EntityId): Entity
    fun setIdGenerator(idSequence: Int)
}
