package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesInternal
import com.google.inject.Inject
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import kotlin.math.max

class ArraysCottaState @Inject constructor(
    private val componentRegistry: ComponentRegistry
) : CottaState {
    private val internal = ArraysDatabase(componentRegistry)

    private var latestTickSet = 0L

    override fun entities(atTick: Long): EntitiesInternal {
        return ArraysEntitiesInternal(internal, atTick)
    }

    override fun advance(tick: Long) {
        internal.advance()
    }

    override fun set(tick: Long, entities: EntitiesInternal) {
        latestTickSet = max(latestTickSet, tick)
        // what is that then: entities are stored in EntitiesStorage and
        // ComponentsStorage.
        // Need to copy arrays.
        // Also need to reach for arrays directly in serialization anyway
        // So perhaps it makes sense to first expose raw data getters?
        TODO()
    }

    override fun wipe() {
        TODO("Not yet implemented")
    }

    override fun setBlank(entities: EntitiesInternal) {
        TODO("Not yet implemented")
    }

    override fun setBlank(tick: Long) {
        TODO("Not yet implemented")
    }

    override fun copyTo(state: CottaState) {
        TODO("Not yet implemented")
    }

    fun registerComponents(componentRegistry: ComponentRegistry) {
        componentRegistry.getAllComponents().forEach { (key, clazz) ->
            internal.componentsStorage.register(key, clazz)
        }
    }
}
