package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesInternal
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry

class ArraysBasedCottaState(
    private val componentRegistry: ComponentRegistry,
    private val stateHistoryLength: Int = 64
) : CottaState {
    private val tick: StateTick = StateTick(0L)

    private val entitiesStorage = DynamicEntitiesStorage(tick)
    private val componentsStorage = ComponentsStorage(tick)
    private val entities = ArraysEntities(entitiesStorage, componentsStorage, componentRegistry)

    override fun entities(atTick: Long): EntitiesInternal {
        if (atTick > tick.tick) {
            throw RuntimeException("Cannot retrieve entities at tick $atTick: latest stored tick is $tick")
        }
        if (atTick < tick.tick - stateHistoryLength) {
            throw RuntimeException(
                "Cannot retrieve entities at tick $atTick: latest stored tick is ${tick.tick} while history length is $stateHistoryLength"
            )
        }
        return TODO()
    }

    override fun advance(tick: Long) {
        this.tick.tick = tick + 1
/*        entities.advance()
        componentsStorage.advance()*/
    }

    override fun set(tick: Long, entities: EntitiesInternal) {
        TODO("Not yet implemented")
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
}
