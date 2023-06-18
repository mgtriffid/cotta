package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.registry.ComponentKey

interface DeltaRecipe {
    val addedEntities : List<EntityRecipe>
    val changedEntities: List<ChangedEntityRecipe>
    val removedEntitiesIds: Set<EntityId>
}

interface StateRecipe {
    val entities : List<EntityRecipe>
}

interface EntityRecipe {
    val entityId: EntityId
    val ownedBy: Entity.OwnedBy
    val components: List<ComponentRecipe>
}

interface ComponentRecipe
interface ComponentDeltaRecipe

/**
 * Allows to reconstruct the whole state of particular Component. Used to transfer snapshot
 * of Entity as a whole when it is discovered or spawned.
 */
interface FullComponentData<T> {
    fun get(): T
}

interface ChangedEntityRecipe {
    val entityId: EntityId
    val changedComponents: List<ComponentDeltaRecipe>
    val addedComponents: List<ComponentRecipe>
    val removedComponents: List<ComponentKey>
}
