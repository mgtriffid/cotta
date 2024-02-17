package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsTraceRecipe
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface StateSnapper<
    SR: StateRecipe,
    DR: DeltaRecipe,
    CEWTR: CreatedEntitiesWithTracesRecipe
    > {
    fun snapState(entities: Entities): SR
    fun snapDelta(prev: Entities, curr: Entities): DR
    fun unpackStateRecipe(entities: Entities, recipe: SR)
    fun unpackDeltaRecipe(entities: Entities, recipe: DR)
    fun snapTrace(trace: CottaTrace): TraceRecipe
    fun unpackTrace(trace: TraceRecipe): CottaTrace
    fun snapCreatedEntitiesWithTraces(
        createdEntities: List<Pair<CottaTrace, EntityId>>,
        associate: Map<AuthoritativeEntityId, PredictedEntityId>
    ): CEWTR
}
