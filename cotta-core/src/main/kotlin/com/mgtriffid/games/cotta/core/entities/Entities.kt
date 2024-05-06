package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesImpl
import com.mgtriffid.games.cotta.core.exceptions.EntityNotExistsException

interface Entities {
    fun create(ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity
    fun get(id: EntityId): Entity?
    // TODO make internal, should not be exposed to developers
    fun currentId(): Int
    fun getOrNotFound(id: EntityId): Entity = get(id) ?: throw EntityNotExistsException("Could not find entity $id")
    fun all(): Collection<Entity>
    fun dynamic(): Collection<Entity>
    // TODO Should not be exposed to developers.
    fun create(id: EntityId, ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity
    fun remove(id: EntityId)
    // TODO should not be accessible for developers. Instead the whole static
    //  state creation should not be any different from regular creation of
    //  entities for the developer.
    fun createStatic(id: EntityId): Entity
    // TODO should not be exposed to developers
    fun setIdGenerator(idSequence: Int)
}
