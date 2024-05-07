package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesInternal
import jakarta.inject.Inject
import jakarta.inject.Named

class LatestEntities @Inject constructor(
    @Named("simulation") private val state: CottaState,
    @Named(SIMULATION) private val tick: TickProvider
) : Entities {
    override fun create(ownedBy: Entity.OwnedBy): Entity {
        return entities().create(ownedBy)
    }

    override fun get(id: EntityId): Entity? {
        return entities().get(id)
    }

    override fun all(): Collection<Entity> {
        return entities().all()
    }

    override fun dynamic(): Collection<Entity> {
        return entities().dynamic()
    }

    override fun remove(id: EntityId) {
        return entities().remove(id)
    }

    private fun entities() = state.entities(tick.tick)
}
