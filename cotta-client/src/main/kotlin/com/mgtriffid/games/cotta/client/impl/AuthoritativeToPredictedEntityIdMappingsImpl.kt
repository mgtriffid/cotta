package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeToPredictedEntityIdMappings
import com.mgtriffid.games.cotta.core.entities.EntityId
import mu.KotlinLogging
import kotlin.math.log

private val logger = KotlinLogging.logger {}

class AuthoritativeToPredictedEntityIdMappingsImpl : AuthoritativeToPredictedEntityIdMappings {
    private val data = HashMap<EntityId, EntityId>() // TODO stricter typing; authoritative to predicted only

    override operator fun set(authoritativeEntityId: EntityId, predictedEntityId: EntityId) {
        logger.debug { "Mapping $authoritativeEntityId to $predictedEntityId"  }
        data[authoritativeEntityId] = predictedEntityId
    }

    override operator fun get(entityId: EntityId): EntityId? {
        return data[entityId]
    }
}