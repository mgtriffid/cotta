package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider

interface TickProvider {
    var tick: Long

    companion object {
        fun getInstance(): TickProvider = AtomicLongTickProvider()
    }
}
