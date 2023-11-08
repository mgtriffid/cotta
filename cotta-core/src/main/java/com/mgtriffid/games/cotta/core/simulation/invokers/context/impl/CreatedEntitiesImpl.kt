package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityTrace
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreatedEntities
import jakarta.inject.Inject
import jakarta.inject.Named

class CreatedEntitiesImpl @Inject constructor(
    private val tick: TickProvider,
    @Named ("historyLength") private val historyLength: Int
): CreatedEntities {
    private val data = Array(historyLength) { CreatedEntitiesForTick(tick.tick) }

    override fun record(createEntityTrace: CreateEntityTrace, entityId: EntityId) {
        val currentTick = tick.tick
        val pos = (currentTick % historyLength).toInt()
        var entities: CreatedEntitiesForTick = data[pos]
        if (entities.tick != currentTick) {
            entities.data.clear()
            data[pos] = CreatedEntitiesForTick(currentTick)
            entities = data[pos]
        }

        entities.record(createEntityTrace, entityId)
    }

    override fun forTick(tick: Long): Map<CreateEntityTrace, EntityId> {
        return data[(tick % historyLength).toInt()].data.toMap()
    }

    private class CreatedEntitiesForTick(val tick: Long) {
        val data = mutableMapOf<CreateEntityTrace, EntityId>()

        fun record(createEntityTrace: CreateEntityTrace, entityId: EntityId) {
            data[createEntityTrace] = entityId
        }
    }
}
