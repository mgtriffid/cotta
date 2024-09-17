package com.mgtriffid.games.cotta.core.entities.arrays.storage

import com.badlogic.gdx.utils.IntMap
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.arrays.StateTick
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.impl.EntityImpl
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry

internal class DynamicEntitiesStorage(
    val tick: StateTick
) {
    val data = IntMap<EntityData>()

    fun advance() {
        data.values().forEach { it.components.advance() }
    }

    fun create(id: Int) {
        data.put(id, getEntityData())
    }

    fun create(id: Int, ownedBy: Entity.OwnedBy) {
        data.put(id, getEntityData(ownedBy))
    }

    private fun getEntityComponents(): EntityComponents {
        return EntityComponents()
    }

    private fun getEntityData(): EntityData {
        return EntityData(getEntityComponents(), Entity.OwnedBy.System)
    }

    private fun getEntityData(ownedBy: Entity.OwnedBy): EntityData {
        return EntityData(getEntityComponents(), ownedBy)
    }

    fun remove(id: EntityId) {
        data.remove(id.id)
    }

    data class Diff(
        val added: MutableList<Int> = ArrayList(),
        val removed: MutableList<Int> = ArrayList()
    )
}
