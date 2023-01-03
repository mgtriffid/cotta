package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.exceptions.EcsRuntimeException

class CottaStateImpl(
    private val historyLength: Int
) : CottaState {
    private var tick = 0L

    private val entitiesArray = Array<Entities?>(historyLength) { null }

    init {
        entitiesArray[0] = Entities.getInstance()
    }

    override fun currentTick(): Long {
        return tick
    }

    override fun entities(): Entities = entities(tick)

    override fun entities(atTick: Long): Entities {
        if (atTick > tick) {
            throw EcsRuntimeException("Cannot retrieve entities at tick $atTick: current tick is $tick")
        }
        if (atTick <= tick - historyLength) {
            throw EcsRuntimeException(
                "Cannot retrieve entities at tick $atTick: current tick is $tick while history length is $historyLength"
            )
        }
        return entitiesArray[(atTick % historyLength).toInt()]!!
    }

    override fun advance() {
        val entities = entities(tick)
        tick++
        entitiesArray[(tick % historyLength).toInt()] = entities.deepCopy()
    }

    private fun Entities.deepCopy(): Entities {
        return if (this is EntitiesImpl) {
            this.deepCopy()
        } else {
            TODO()
        }
    }
}
