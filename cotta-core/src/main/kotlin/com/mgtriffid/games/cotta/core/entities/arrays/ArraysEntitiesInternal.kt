package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesInternal

class ArraysEntitiesInternal(
    private val internal: ArraysDatabase,
    private val tick: Long
) : EntitiesInternal {
    override fun currentId(): Int {
        TODO("Not yet implemented")
    }

    // Used in PredictionCreationStrategy (why?)
    // Used in BytesSnapper - should be done differently, we're going to pass
    // nearly raw data over network, then we'll push arrays into arrays and be
    // happy
    // Used in PredictedLatestEntities - why?
    // It's not completely clear why do we have special IDs of Entities in prediction.
    override fun create(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
        TODO("Not yet implemented")
    }

    override fun create(ownedBy: Entity.OwnedBy): Entity {
        return internal.createEntity(ownedBy)
    }

    override fun setIdGenerator(idSequence: Int) {
        TODO("Not yet implemented")
    }

    override fun deepCopy(): EntitiesInternal {
        TODO("Not yet implemented")
    }

    override fun get(id: EntityId): Entity? {
        return internal.atTick(tick).getEntity(id)
    }

    override fun all(): Collection<Entity> {
        return internal.atTick(tick).all()
    }

    override fun dynamic(): Collection<Entity> {
        TODO("Not yet implemented")
    }

    override fun remove(id: EntityId) {
        TODO("Not yet implemented")
    }
}
