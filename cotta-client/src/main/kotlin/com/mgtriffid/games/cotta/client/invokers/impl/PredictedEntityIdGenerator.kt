package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId

interface PredictedEntityIdGenerator {
    fun getId(): PredictedEntityId
}
