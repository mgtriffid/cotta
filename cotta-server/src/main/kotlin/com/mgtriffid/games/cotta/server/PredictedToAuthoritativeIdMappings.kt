package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId

interface PredictedToAuthoritativeIdMappings {
    fun record(predictedEntityId: PredictedEntityId, id: AuthoritativeEntityId)
    fun forTick(tick: Long): List<Pair<PredictedEntityId, AuthoritativeEntityId>>
    operator fun get(predictedEntityId: PredictedEntityId): AuthoritativeEntityId?
}
