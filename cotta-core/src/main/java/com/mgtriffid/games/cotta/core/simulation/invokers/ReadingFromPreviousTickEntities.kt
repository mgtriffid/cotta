package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.*
import jakarta.inject.Named

class ReadingFromPreviousTickEntities(
    private val sawTickHolder: SawTickHolder,
    private val state: CottaState,
    private val tickProvider: TickProvider
) : Entities {
    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return getEntities().createEntity(ownedBy)
    }

    override fun get(id: EntityId): Entity {
        val entities = sawTickHolder.tick?.let { state.entities(atTick = it) } ?: getEntities()
        return entities.get(id)
    }

    override fun all(): Collection<Entity> {
        val entities = sawTickHolder.tick?.let { state.entities(atTick = it) } ?: getEntities()
        return entities.all()
    }

    private fun getEntities() = state.entities(tickProvider.tick)

    override fun createEntity(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
        throw NotImplementedError("Is not supposed to be called on Server")
    }

    override fun remove(id: EntityId) {
        // TODO should be actually handled by a different subclass
        throw NotImplementedError("Is not supposed to be called on Server")
    }
}