package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreatedEntities
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import jakarta.inject.Inject
import jakarta.inject.Named

class CreatedEntitiesImpl @Inject constructor(
    private val tick: TickProvider,
    @Named ("historyLength") private val historyLength: Int
): CreatedEntities {
    // GROOM set empty CreatedEntitiesForTick if no entities were created
    private val data = Array(historyLength) { CreatedEntitiesForTick(tick.tick) }

    override fun record(trace: CottaTrace, entityId: EntityId) {
        val currentTick = tick.tick
        val pos = (currentTick % historyLength).toInt()
        var entities: CreatedEntitiesForTick = data[pos]
        if (entities.tick != currentTick) {
            entities.data.clear()
            data[pos] = CreatedEntitiesForTick(currentTick)
            entities = data[pos]
        }

        entities.record(trace, entityId)
    }

    override fun forTick(tick: Long): List<Pair<CottaTrace, EntityId>> {
        return data[(tick % historyLength).toInt()].data
    }

    private class CreatedEntitiesForTick(val tick: Long) {
        val data = ArrayList<Pair<CottaTrace, EntityId>>()

        fun record(trace: CottaTrace, entityId: EntityId) {
            data.add(Pair(trace, entityId))
        }
    }
}
