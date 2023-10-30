package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId

class ReadingFromPreviousTickEntities(
    private val sawTickHolder: SawTickHolder,
    private val state: CottaState
) : Entities {
    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return state.entities().createEntity(ownedBy)
    }

    override fun get(id: EntityId): Entity {
        val entities = sawTickHolder.tick?.let { state.entities(atTick = it) } ?: state.entities()
        return entities.get(id)
    }

    override fun all(): Collection<Entity> {
        val entities = sawTickHolder.tick?.let { state.entities(atTick = it) } ?: state.entities()
        return entities.all()
    }

    override fun createEntity(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
        throw NotImplementedError("Is not supposed to be called on Server")
    }

    override fun remove(id: EntityId) {
        // TODO should be actually handled by a different subclass
        throw NotImplementedError("Is not supposed to be called on Server")
    }
}