package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.registry.ComponentKey

interface DeltaRecipe {
    val addedEntities : List<EntityRecipe>
    val changedEntities: List<ChangedEntityRecipe>
    val removedEntitiesIds: Set<EntityId>
}

data class Delta(
    val removedEntitiesIds: Set<EntityId>,
    val addedEntities: List<Entity>,
    val changedEntities: List<Entity>,
    val tick: Long
)

interface StateRecipe {
    val entities : List<EntityRecipe>
}

interface EntityRecipe {
    val entityId: EntityId
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

/**
 * Allows to modify part of Component state. For example when something is moved to the left
 * then this allows to transfer change of xPos but leave yPos intact.
 */
interface ChangedComponentData<T> {
    fun apply(to: T)
}

interface ChangedEntityRecipe {
    val entityId: EntityId
    val changedComponents: List<ComponentDeltaRecipe>
    val addedComponents: List<ComponentRecipe>
    val removedComponents: List<ComponentKey>
}
