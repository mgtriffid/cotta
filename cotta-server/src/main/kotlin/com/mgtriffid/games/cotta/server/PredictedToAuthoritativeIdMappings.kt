package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.PredictedEntityId

interface PredictedToAuthoritativeIdMappings {
    fun record(predictedEntityWithSimilarTrace: PredictedEntityId, id: EntityId)
}