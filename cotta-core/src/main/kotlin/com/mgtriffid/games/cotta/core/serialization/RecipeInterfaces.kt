package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.registry.ComponentKey

interface DeltaRecipe {
    val addedEntities : List<EntityRecipe>
    val changedEntities: List<ChangedEntityRecipe>
    val removedEntitiesIds: Set<EntityId>
    val idSequence: Int
}

interface StateRecipe {
    val entities : List<EntityRecipe>
}

interface MetaEntitiesDeltaRecipe {
    val addedPlayers : List<PlayerId>
}

interface EntityRecipe {
    val entityId: EntityId
    val ownedBy: Entity.OwnedBy
    val components: List<ComponentRecipe>
}

interface ComponentRecipe
interface ComponentDeltaRecipe

interface ChangedEntityRecipe {
    val entityId: EntityId
    val changedComponents: List<ComponentDeltaRecipe>
    val addedComponents: List<ComponentRecipe>
    val removedComponents: List<ComponentKey>
}

interface InputRecipe {
    val entityInputs: List<EntityInputRecipe>
}

interface EntityInputRecipe {
    val entityId: EntityId
    val inputComponents: List<InputComponentRecipe>
}

interface InputComponentRecipe

interface EffectRecipe

interface TraceRecipe {
    val elements: List<TraceElementRecipe>
}

interface TraceElementRecipe
interface CreatedEntitiesWithTracesRecipe {
    val mappedPredictedIds: Map<AuthoritativeEntityId, PredictedEntityId>
    val traces: List<Pair<TraceRecipe, EntityId>>
}
