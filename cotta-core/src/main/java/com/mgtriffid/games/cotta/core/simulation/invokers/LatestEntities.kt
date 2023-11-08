package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import jakarta.inject.Inject

class LatestEntities @Inject constructor(private val state: CottaState) : Entities {
   override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
       return state.entities().createEntity(ownedBy)
   }

   override fun get(id: EntityId): Entity {
       return state.entities().get(id)
   }

   override fun all(): Collection<Entity> {
       return state.entities().all()
   }

   override fun remove(id: EntityId) {
       throw NotImplementedError("Is not supposed to be called on Server")
   }

   override fun createEntity(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
       return state.entities().createEntity(id, ownedBy)
   }
}