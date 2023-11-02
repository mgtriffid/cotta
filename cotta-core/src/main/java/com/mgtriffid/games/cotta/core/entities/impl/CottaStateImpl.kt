package com.mgtriffid.games.cotta.core.entities.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.exceptions.EcsRuntimeException
import jakarta.inject.Named

class CottaStateImpl @Inject constructor(
    @Named("historyLength") private val historyLength: Int,
    private val tick: TickProvider
) : CottaState {

    private val entitiesArray = Array<Entities?>(historyLength) { null }

    init {
        entitiesArray[0] = Entities.getInstance()
    }

    override fun entities(): Entities = entities(tick.tick)

    override fun entities(atTick: Long): Entities {
        if (atTick > tick.tick) {
            throw EcsRuntimeException("Cannot retrieve entities at tick $atTick: current tick is $tick")
        }
        if (atTick < tick.tick - historyLength) {
            throw EcsRuntimeException(
                "Cannot retrieve entities at tick $atTick: current tick is ${tick.tick} while history length is $historyLength"
            )
        }
        return entitiesArray[(atTick % historyLength).toInt()]!!
    }

    override fun advance() {
        val entities = entities(tick.tick)
        tick.tick++ // TODO move out of here
        entitiesArray[(tick.tick % historyLength).toInt()] = entities.deepCopy()
    }

    override fun setBlank(tick: Long) {
        entitiesArray[(tick % historyLength).toInt()] = Entities.getInstance()
    }

    private fun Entities.deepCopy(): Entities {
        return if (this is EntitiesImpl) {
            this.deepCopy()
        } else {
            TODO()
        }
    }
}
