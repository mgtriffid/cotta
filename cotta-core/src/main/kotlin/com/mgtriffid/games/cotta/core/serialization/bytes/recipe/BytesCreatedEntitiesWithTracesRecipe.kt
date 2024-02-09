package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId

class BytesCreatedEntitiesWithTracesRecipe(
    val traces: List<Pair<BytesTraceRecipe, EntityId>>,
    val mappedPredictedIds: Map<AuthoritativeEntityId, PredictedEntityId>
)
