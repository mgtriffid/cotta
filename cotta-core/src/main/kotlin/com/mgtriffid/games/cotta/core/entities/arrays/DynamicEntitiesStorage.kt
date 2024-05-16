package com.mgtriffid.games.cotta.core.entities.arrays

import com.badlogic.gdx.utils.IntMap
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.impl.EntityImpl
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry

internal class DynamicEntitiesStorage(
    val tick: StateTick
) {
    val data = IntMap<EntityComponents>()
    private val justCreated = IntMap<EntityImpl>()

    /**
     * Diffs between ticks. These are needed for lag compensation.
     */
    val diffs = ArrayList<Diff>(16)

    /**
     * Do we need to store removals/entities in a diff? I mean iteration over
     * _all_ entities in a previous tick?.. Alright let's do. May be useful.
     * At least even if it's not used and even if it doesn't perform particularly
     * well, we have to not confuse a developer. If they write a LagCompensated
     * system - it should just work.
     */
    val limboEntities = IntMap<EntityComponents>()

    fun advance(tick: Long) {

    }

    fun createInternal(id: Int) {
        data.put(id, getEntityComponents())
    }


    /**
     * This is required in cases when an Entity is created while entities are
     * locked because of iteration.
     */
    fun createTemporary(componentRegistry: ComponentRegistry, id: EntityId, ownedBy: Entity.OwnedBy): EntityImpl {
        val ret = EntityImpl(componentRegistry, id, ownedBy)
        justCreated.put(id.id, ret)
        return ret
    }

    private fun getEntityComponents(): EntityComponents {
        return EntityComponents()
    }

    data class Diff(
        val added: MutableList<Int> = ArrayList(),
        val removed: MutableList<Int> = ArrayList()
    )
}
