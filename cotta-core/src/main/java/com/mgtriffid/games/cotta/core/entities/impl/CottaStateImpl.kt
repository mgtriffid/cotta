package com.mgtriffid.games.cotta.core.entities.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.exceptions.EcsRuntimeException
import jakarta.inject.Named
import kotlin.math.max

class CottaStateImpl @Inject constructor(
    @Named("historyLength") private val historyLength: Int
) : CottaState {

    private val entitiesArray = Array<Entities?>(historyLength) { null }

    private var latestTickSet = 0L

    init {
        entitiesArray[0] = Entities.getInstance()
    }

    override fun entities(atTick: Long): Entities {
        if (atTick > latestTickSet) {
            throw EcsRuntimeException("Cannot retrieve entities at tick $atTick: latest stored tick is $latestTickSet")
        }
        if (atTick < latestTickSet - historyLength) {
            throw EcsRuntimeException(
                "Cannot retrieve entities at tick $atTick: latest stored tick is $latestTickSet while history length is $historyLength"
            )
        }
        return entitiesArray[atTick.toIndex()]!!
    }

    override fun advance(tick: Long) {
        val entities = entities(tick)
        set(tick + 1, entities.deepCopy())
    }

    override fun set(tick: Long, entities: Entities) {
        entitiesArray[tick.toIndex()] = entities
        latestTickSet = max(latestTickSet, tick)
    }

    override fun wipe() {
        entitiesArray.fill(null)
        latestTickSet = 0L
    }

    private fun Long.toIndex() = (this % historyLength).toInt()

    private fun Entities.deepCopy(): Entities {
        return if (this is EntitiesImpl) {
            this.deepCopy()
        } else {
            TODO()
        }
    }
}
