package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.PredictedEntityId

interface PredictedToAuthoritativeIdMappings {
    fun record(predictedEntityId: PredictedEntityId, id: EntityId)
    fun forTick(tick: Long): List<Pair<PredictedEntityId, AuthoritativeEntityId>>
    operator fun get(predictedEntityId: PredictedEntityId): EntityId?
}