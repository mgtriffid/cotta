package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId

interface AuthoritativeToPredictedEntityIdMappings {
    operator fun get(entityId: EntityId): EntityId?
    operator fun set(authoritativeEntityId: AuthoritativeEntityId, predictedEntityId: PredictedEntityId)
    fun all(): Map<AuthoritativeEntityId, PredictedEntityId>
}
