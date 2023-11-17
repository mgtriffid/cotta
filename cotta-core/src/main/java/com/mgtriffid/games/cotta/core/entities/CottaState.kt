package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl

interface CottaState {
    companion object {
        fun getInstance(tickProvider: TickProvider): CottaState = CottaStateImpl(64)
    }
    fun entities(atTick: Long): Entities

    fun advance(tick: Long)

    fun set(tick: Long, entities: Entities)
}
