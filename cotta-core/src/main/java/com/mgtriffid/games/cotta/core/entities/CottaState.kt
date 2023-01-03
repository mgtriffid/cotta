package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl

interface CottaState {
    companion object {
        fun getInstance(): CottaState = CottaStateImpl(8)
    }
    fun currentTick(): Long
    fun entities(): Entities
    fun entities(atTick: Long): Entities
    fun advance()
}
