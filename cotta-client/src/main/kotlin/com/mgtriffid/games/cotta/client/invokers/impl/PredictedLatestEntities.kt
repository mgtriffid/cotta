package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.entities.*
import jakarta.inject.Inject
import jakarta.inject.Named

class PredictedLatestEntities @Inject constructor(
    @Named("prediction") private val state: CottaState,
    @Named("prediction") private val tickProvider: TickProvider
): Entities {
    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return entities().createEntity(ownedBy)
    }

    override fun get(id: EntityId): Entity {
        return entities().get(id)
    }

    override fun all(): Collection<Entity> {
        return entities().all()
    }

    override fun remove(id: EntityId) {
        throw NotImplementedError("Is not supposed to be called on Server") // stupid comment btw
    }

    override fun createEntity(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
        return entities().createEntity(id, ownedBy)
    }

    private fun entities() = state.entities(tickProvider.tick)
}
