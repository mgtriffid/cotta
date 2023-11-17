package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.*
import jakarta.inject.Inject
import jakarta.inject.Named

class LatestEntities @Inject constructor(
    @Named("simulation") private val state: CottaState,
    private val tick: TickProvider
) : Entities {
    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return entities().createEntity(ownedBy)
    }

    private fun entities() = state.entities(tick.tick)

    override fun get(id: EntityId): Entity {
       return entities().get(id)
   }

   override fun all(): Collection<Entity> {
       return entities().all()
   }

   override fun remove(id: EntityId) {
       throw NotImplementedError("Is not supposed to be called on Server")
   }

   override fun createEntity(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
       return entities().createEntity(id, ownedBy)
   }
}