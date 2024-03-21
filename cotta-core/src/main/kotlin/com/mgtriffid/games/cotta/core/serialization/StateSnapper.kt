package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface StateSnapper<
    SR: StateRecipe,
    DR: DeltaRecipe,
    CEWTR: CreatedEntitiesWithTracesRecipe,
    MEDR: MetaEntitiesDeltaRecipe
    > {
    fun snapState(entities: Entities): SR
    fun unpackStateRecipe(entities: Entities, recipe: SR)
    fun snapDelta(prev: Entities, curr: Entities): DR
    fun unpackDeltaRecipe(entities: Entities, recipe: DR)
    fun snapPlayersDelta(
        addedPlayers: List<PlayerId>,
//        removedEntitiesIds: Set<EntityId>
    ): MEDR
    fun unpackPlayersDeltaRecipe(recipe: MEDR): List<PlayerId>
    fun snapTrace(trace: CottaTrace): TraceRecipe
    fun unpackTrace(trace: TraceRecipe): CottaTrace
    fun snapCreatedEntitiesWithTraces(
        createdEntities: List<Pair<CottaTrace, EntityId>>,
        associate: Map<AuthoritativeEntityId, PredictedEntityId>
    ): CEWTR
}
