package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.id.EntityId

class ReadingFromPreviousTickEntities(
    private val sawTickHolder: SawTickHolder,
    private val state: CottaState,
    private val tickProvider: TickProvider
) : Entities {
    override fun create(ownedBy: Entity.OwnedBy): Entity {
        return getEntities().create(ownedBy)
    }

    override fun get(id: EntityId): Entity? {
        val entities = sawTickHolder.tick?.let { state.entities(atTick = it) } ?: getEntities()
        return entities.get(id)
    }

    override fun all(): Collection<Entity> {
        val entities = sawTickHolder.tick?.let { state.entities(atTick = it) } ?: getEntities()
        return entities.all()
    }

    override fun dynamic(): Collection<Entity> {
        val entities = sawTickHolder.tick?.let { state.entities(atTick = it) } ?: getEntities()
        return entities.dynamic()
    }

    private fun getEntities() = state.entities(tickProvider.tick)

    override fun remove(id: EntityId) {
        // TODO should be actually handled by a different subclass
        getEntities().remove(id)
    }
}
