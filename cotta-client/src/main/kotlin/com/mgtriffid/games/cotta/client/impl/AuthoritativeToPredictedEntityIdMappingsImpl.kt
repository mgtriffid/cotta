package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeToPredictedEntityIdMappings
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// TODO clear old mappings
class AuthoritativeToPredictedEntityIdMappingsImpl : AuthoritativeToPredictedEntityIdMappings {
    private val data = HashMap<AuthoritativeEntityId, PredictedEntityId>()

    override operator fun set(authoritativeEntityId: AuthoritativeEntityId, predictedEntityId: PredictedEntityId) {
        logger.debug { "Mapping $authoritativeEntityId to $predictedEntityId"  }
        data[authoritativeEntityId] = predictedEntityId
    }

    override operator fun get(entityId: EntityId): EntityId? {
        return data[entityId]
    }

    override fun all(): Map<AuthoritativeEntityId, PredictedEntityId> {
        val ret = HashMap<AuthoritativeEntityId, PredictedEntityId>()
        data.forEach { (k, v) ->
            ret[k] = v
        }
        return ret
    }
}
