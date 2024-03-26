package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import jakarta.inject.Inject
import jakarta.inject.Named

class PredictedLatestEntities @Inject constructor(
    @Named("prediction") private val state: CottaState,
    @Named("prediction") private val tickProvider: TickProvider
): Entities {
    override fun create(ownedBy: Entity.OwnedBy): Entity {
        return entities().create(ownedBy)
    }

    override fun get(id: EntityId): Entity? {
        return entities().get(id)
    }

    override fun all(): Collection<Entity> {
        return entities().all()
    }

    override fun currentId(): Int {
        return entities().currentId()
    }

    override fun dynamic(): Collection<Entity> {
        return entities().dynamic()
    }

    override fun remove(id: EntityId) {
        entities().remove(id)
    }

    override fun create(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
        return entities().create(id, ownedBy)
    }

    override fun createStatic(id: EntityId): Entity {
        throw IllegalStateException("Cannot create static entity while running the game")
    }

    override fun setIdGenerator(idSequence: Int) {
        entities().setIdGenerator(idSequence)
    }

    private fun entities() = state.entities(tickProvider.tick)
}
