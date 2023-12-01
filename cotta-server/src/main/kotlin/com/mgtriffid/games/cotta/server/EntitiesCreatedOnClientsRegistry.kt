package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.PredictedEntityId
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.server.impl.ClientsPredictedEntities

interface EntitiesCreatedOnClientsRegistry {
    fun find(trace: CottaTrace): PredictedEntityId?
    fun populate(predictedClientEntities: ClientsPredictedEntities)
}