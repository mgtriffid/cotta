package com.mgtriffid.games.cotta.server


interface ServerSimulationInputProvider {
    fun fetch()
    fun getDelta() : ServerDelta
}
