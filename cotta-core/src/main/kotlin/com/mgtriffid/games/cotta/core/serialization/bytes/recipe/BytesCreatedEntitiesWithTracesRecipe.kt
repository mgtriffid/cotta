package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.serialization.CreatedEntitiesWithTracesRecipe

class BytesCreatedEntitiesWithTracesRecipe(
    override val traces: List<Pair<BytesTraceRecipe, EntityId>>,
    override val mappedPredictedIds: Map<AuthoritativeEntityId, PredictedEntityId>
) : CreatedEntitiesWithTracesRecipe
