package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.serialization.IdsRemapper
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import jakarta.inject.Inject
import jakarta.inject.Named
import jdk.jfr.Name

const val MAX_TICKS_TO_KEEP = 128

class PredictedCreatedEntitiesRegistryImpl @Inject constructor(
    private val idsRemapper: IdsRemapper,
    @Named("localInput") private val localInputTickProvider: TickProvider
) : PredictedCreatedEntitiesRegistry {
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

    override fun latest(): List<Pair<CottaTrace, EntityId>> {
        return find(localInputTickProvider.tick)
    }

    override fun useAuthoritativeEntitiesWherePossible(mappings: Map<AuthoritativeEntityId, PredictedEntityId>) {
        val predictedToAuthoritativeEntityId = mappings.entries.associate { it.value to it.key }
        data.replaceAll { (key, entityId) ->
            Pair(remapKey(key, predictedToAuthoritativeEntityId), entityId)
        }
    }

    private fun remapKey(key: Key, mappings: Map<PredictedEntityId, AuthoritativeEntityId>): Key {
        val newTrace = idsRemapper.remapTrace(key.trace) { p -> mappings[p] }
        return if (key.trace === newTrace) {
            key
        } else {
            Key(key.tick, newTrace)
        }
    }

    private fun cleanUpOld(tick: Long) {
        data.removeIf { it.first.tick < tick - MAX_TICKS_TO_KEEP }
    }

    private data class Key(
        val tick: Long,
        val trace: CottaTrace
    )
}
