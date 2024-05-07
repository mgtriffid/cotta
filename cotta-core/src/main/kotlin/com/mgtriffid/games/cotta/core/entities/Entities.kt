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

    fun all(): Collection<Entity>
    fun dynamic(): Collection<Entity>
    fun remove(id: EntityId)
}
