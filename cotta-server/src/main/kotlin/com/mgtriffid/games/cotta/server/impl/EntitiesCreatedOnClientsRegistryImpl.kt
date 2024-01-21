package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.serialization.IdsRemapper
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.server.EntitiesCreatedOnClientsRegistry
import com.mgtriffid.games.cotta.server.PredictedToAuthoritativeIdMappings
import jakarta.inject.Inject

class EntitiesCreatedOnClientsRegistryImpl @Inject constructor(
    private val predictedToAuthoritativeIdMappings: PredictedToAuthoritativeIdMappings,
    private val idsRemapper: IdsRemapper
) : EntitiesCreatedOnClientsRegistry {
    private val data = HashMap<CottaTrace, PredictedEntityId>()
    override fun find(trace: CottaTrace): PredictedEntityId? {
        return data[trace]
    }

    override fun populate(predictedClientEntities: ClientsPredictedEntities) {
        predictedClientEntities.createdEntities.forEach { (trace, id) -> data[idsRemapper.remapTrace(trace, predictedToAuthoritativeIdMappings::get)] = id }
    }
}
