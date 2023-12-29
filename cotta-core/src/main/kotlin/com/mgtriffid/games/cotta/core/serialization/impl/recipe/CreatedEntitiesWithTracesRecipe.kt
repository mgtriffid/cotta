package com.mgtriffid.games.cotta.core.serialization.impl.recipe

import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId

class CreatedEntitiesWithTracesRecipe(
    val traces: List<Pair<MapsTraceRecipe, EntityId>>,
    val mappedPredictedIds: Map<AuthoritativeEntityId, PredictedEntityId>
)
