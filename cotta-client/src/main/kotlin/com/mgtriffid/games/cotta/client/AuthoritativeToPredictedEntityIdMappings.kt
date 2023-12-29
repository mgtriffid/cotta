package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface AuthoritativeToPredictedEntityIdMappings {
    operator fun get(entityId: EntityId): EntityId?
    operator fun set(authoritativeEntityId: EntityId, predictedEntityId: EntityId)
}