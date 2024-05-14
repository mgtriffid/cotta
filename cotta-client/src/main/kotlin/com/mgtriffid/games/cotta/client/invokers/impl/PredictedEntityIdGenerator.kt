package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface PredictedEntityIdGenerator {
    fun getId(): EntityId
}
