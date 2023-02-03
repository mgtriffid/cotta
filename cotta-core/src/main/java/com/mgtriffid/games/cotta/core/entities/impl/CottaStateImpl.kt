package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.exceptions.EcsRuntimeException

class CottaStateImpl(
    private val historyLength: Int,
    // TODO doesn't have to be a dependency actually
    private val tick: TickProvider
) : CottaState {

    private val entitiesArray = Array<Entities?>(historyLength) { null }

    init {
        entitiesArray[0] = Entities.getInstance()
    }

    override fun currentTick(): Long {
        return tick.tick
    }

    override fun entities(): Entities = entities(tick.tick)

    override fun entities(atTick: Long): Entities {
        if (atTick > tick.tick) {
            throw EcsRuntimeException("Cannot retrieve entities at tick $atTick: current tick is $tick")
        }
        if (atTick <= tick.tick - historyLength) {
            throw EcsRuntimeException(
                "Cannot retrieve entities at tick $atTick: current tick is $tick while history length is $historyLength"
            )
        }
        return entitiesArray[(atTick % historyLength).toInt()]!!
    }

    override fun advance() {
        val entities = entities(tick.tick)
        tick.tick++ // TODO move out of here
        entitiesArray[(tick.tick % historyLength).toInt()] = entities.deepCopy()
    }

    private fun Entities.deepCopy(): Entities {
        return if (this is EntitiesImpl) {
            this.deepCopy()
        } else {
            TODO()
        }
    }
}
