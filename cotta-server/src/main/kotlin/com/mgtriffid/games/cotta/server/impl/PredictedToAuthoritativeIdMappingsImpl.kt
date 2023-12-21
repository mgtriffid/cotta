package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.PredictedEntityId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.server.PredictedToAuthoritativeIdMappings
import jakarta.inject.Inject
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

// TODO make it historical and not store too old mappings
//      or even better too old mappings plus those acknowledged by clients
class PredictedToAuthoritativeIdMappingsImpl @Inject constructor(
    private val tickProvider: TickProvider
) : PredictedToAuthoritativeIdMappings {
    private val data = TreeMap<Long, MutableMap<PredictedEntityId, EntityId>>()

    // TODO cleanup old too
    private val all = HashMap<PredictedEntityId, EntityId>()

    override fun record(predictedEntityId: PredictedEntityId, id: EntityId) {
        logger.debug { "Recorded mapping $predictedEntityId -> $id" }
        data.computeIfAbsent(tickProvider.tick) { HashMap() }[predictedEntityId] = id
        all[predictedEntityId] = id
        cleanUpOld()
    }

    override fun forTick(tick: Long): List<Pair<PredictedEntityId, AuthoritativeEntityId>> {
        return data[tick]?.map { (predicted, authoritative) ->
            predicted to authoritative as AuthoritativeEntityId
        } ?: emptyList()
    }

    override operator fun get(predictedEntityId: PredictedEntityId) = all[predictedEntityId]

    private fun cleanUpOld() {
        val tick = tickProvider.tick
        data.entries.removeAll { it.key < tick - 100 }
    }
}
