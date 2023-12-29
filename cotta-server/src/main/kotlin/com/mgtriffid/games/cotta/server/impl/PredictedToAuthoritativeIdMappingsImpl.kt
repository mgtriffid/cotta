package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.server.PredictedToAuthoritativeIdMappings
import jakarta.inject.Inject
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

class PredictedToAuthoritativeIdMappingsImpl @Inject constructor(
    private val tickProvider: TickProvider
) : PredictedToAuthoritativeIdMappings {
    private val data = TreeMap<Long, MutableMap<PredictedEntityId, EntityId>>()

    private val all = HashMap<PredictedEntityId, EntityId>()

    override fun record(predictedEntityId: PredictedEntityId, id: EntityId) {
        logger.trace { "Recorded mapping $predictedEntityId -> $id" }
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
        if (data.firstKey() > tick - 128) return
        data.navigableKeySet().subSet(data.firstKey(), tick - 128).toList().forEach {
            logger.debug { "Removing data for tick $it" }
            data[it]?.forEach { (predicted, _) ->
                all.remove(predicted)
            }
            data.remove(it)
        }
    }
}
