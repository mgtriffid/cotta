package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry

interface CottaState {
    companion object {
        fun getInstance(componentRegistry: ComponentRegistry): CottaState = CottaStateImpl(
            componentRegistry,
            64
        )
    }
    fun entities(atTick: Long): Entities

    fun advance(tick: Long)

    fun set(tick: Long, entities: Entities)

    fun wipe() // maybe not needed for Simulation; maybe need to extract a separate interface for Predicted

    fun setBlank(entities: Entities)

    fun setBlank(tick: Long)

    fun copyTo(state: CottaState)
}
