package com.mgtriffid.games.cotta.core.serialization.impl.recipe

import com.mgtriffid.games.cotta.core.entities.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.PredictedEntityId

class CreatedEntitiesWithTracesRecipe(
    val traces: List<Pair<MapsTraceRecipe, EntityId>>,
    val mappedPredictedIds: Map<AuthoritativeEntityId, PredictedEntityId>
)
