package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.PredictedEntityId
import com.mgtriffid.games.cotta.server.PredictedToAuthoritativeIdMappings

class PredictedToAuthoritativeIdMappingsImpl : PredictedToAuthoritativeIdMappings {
    private val data = HashMap<PredictedEntityId, EntityId>()
    override fun record(predictedEntityWithSimilarTrace: PredictedEntityId, id: EntityId) {
        data[predictedEntityWithSimilarTrace] = id
    }
}