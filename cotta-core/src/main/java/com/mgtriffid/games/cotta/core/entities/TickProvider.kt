package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider

// GROOM unified name. Sometimes it's tick, sometimes tickProvider.
interface TickProvider {
    var tick: Long

    companion object {
        fun getInstance(): TickProvider = AtomicLongTickProvider()
    }
}
