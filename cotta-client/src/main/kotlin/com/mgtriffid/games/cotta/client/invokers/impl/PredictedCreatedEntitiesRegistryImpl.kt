package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

const val MAX_TICKS_TO_KEEP = 128
class PredictedCreatedEntitiesRegistryImpl : PredictedCreatedEntitiesRegistry {
    private val data = ArrayList<Pair<Key, EntityId>>()
    override fun record(trace: CottaTrace, tick: Long, entityId: EntityId) {
        data.add(Pair(Key(tick, trace), entityId))
        cleanUpOld(tick)
    }

    override fun find(trace: CottaTrace, tick: Long): EntityId? {
        return data.find { it.first == Key(tick, trace) }?.second
    }

    override fun find(tick: Long): List<Pair<CottaTrace, EntityId>> {
        return data.filter { it.first.tick == tick }.map { Pair(it.first.trace, it.second) }
    }

    private fun cleanUpOld(tick: Long) {
        data.removeIf { it.first.tick < tick - MAX_TICKS_TO_KEEP }
    }

    private data class Key(
        val tick: Long,
        val trace: CottaTrace
    )
}
