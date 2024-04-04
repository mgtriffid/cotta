package com.mgtriffid.games.cotta.core.entities.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.exceptions.EcsRuntimeException
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import jakarta.inject.Named
import kotlin.math.max

private val logger = mu.KotlinLogging.logger {}

class CottaStateImpl @Inject constructor(
    private val componentRegistry: ComponentRegistry,
    @Named("stateHistoryLength") private val stateHistoryLength: Int
) : CottaState {

    private lateinit var blank: Entities

    private val entitiesArray = Array<Entities?>(stateHistoryLength) { null }

    private var latestTickSet = 0L

    init {
        entitiesArray[0] = EntitiesImpl(componentRegistry)
    }

    override fun entities(atTick: Long): Entities {
        if (atTick > latestTickSet) {
            throw EcsRuntimeException("Cannot retrieve entities at tick $atTick: latest stored tick is $latestTickSet")
        }
        if (atTick < latestTickSet - stateHistoryLength) {
            throw EcsRuntimeException(
                "Cannot retrieve entities at tick $atTick: latest stored tick is $latestTickSet while history length is $stateHistoryLength"
            )
        }
        return entitiesArray[atTick.toIndex()]!!
    }

    override fun advance(tick: Long) {
        logger.debug { "Advancing state tick from $tick to ${tick + 1}" }
        val entities = entities(tick)
        set(tick + 1, entities.deepCopy())
    }

    override fun set(tick: Long, entities: Entities) {
        entitiesArray[tick.toIndex()] = entities
        latestTickSet = max(latestTickSet, tick)
    }

    override fun setBlank(tick: Long) {
        set(tick, blank.deepCopy())
    }

    override fun wipe() {
        entitiesArray.fill(null)
        latestTickSet = 0L
    }

    override fun setBlank(entities: Entities) {
        this.blank = entities.deepCopy()
    }

    override fun copyTo(state: CottaState) {
        state.set(tick = latestTickSet, entities = entities(latestTickSet).deepCopy())
    }

    private fun Long.toIndex() = (this % stateHistoryLength).toInt()

    private fun Entities.deepCopy(): Entities {
        return if (this is EntitiesImpl) {
            this.deepCopy()
        } else {
            TODO()
        }
    }
}
