package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.PredictedEntityId
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.server.EntitiesCreatedOnClientsRegistry

class EntitiesCreatedOnClientsRegistryImpl : EntitiesCreatedOnClientsRegistry {
    private val data = HashMap<CottaTrace, PredictedEntityId>()
    override fun find(trace: CottaTrace): PredictedEntityId? {
        return data[trace]
    }

    override fun populate(predictedClientEntities: ClientsPredictedEntities) {
        predictedClientEntities.createdEntities.forEach { (trace, id) -> data[trace] = id }
    }
}