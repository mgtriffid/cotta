package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.PlayerId


interface ServerSimulationInputProvider {
    fun fetch()
    fun getDelta() : ServerDelta
    fun bufferAheadLength(playerId: PlayerId) : Int
}
