package com.mgtriffid.games.cotta.core.serialization.maps.recipe

import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.serialization.CreatedEntitiesWithTracesRecipe

class MapsCreatedEntitiesWithTracesRecipe(
    val traces: List<Pair<MapsTraceRecipe, EntityId>>,
    val mappedPredictedIds: Map<AuthoritativeEntityId, PredictedEntityId>
) : CreatedEntitiesWithTracesRecipe
