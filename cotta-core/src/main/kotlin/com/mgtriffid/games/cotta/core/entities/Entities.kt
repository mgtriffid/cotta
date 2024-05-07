package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.exceptions.EntityNotExistsException

/**
 * Represents all Entities in game world in a current tick.
 */
interface Entities {
    /**
     * Creates a new Entity owned by the given owner (defaults to System).
     *
     * @param ownedBy the owner of the entity - means this entity will be
     * processed during prediction on the client side if the owner is a Player,
     * otherwise it's a regular Entity not processed by prediction.
     */
    fun create(ownedBy: Entity.OwnedBy = Entity.OwnedBy.System): Entity

    /**
     * Retrieves an Entity by its id.
     */
    fun get(id: EntityId): Entity?
    fun getOrNotFound(id: EntityId): Entity = get(id) ?: throw EntityNotExistsException("Could not find entity $id")

    // TODO make internal, should not be exposed to developers
    fun currentId(): Int
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
