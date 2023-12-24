package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.simulation.SimulationInput

interface ServerSimulationInputProvider {
    fun prepare()
    fun get(): SimulationInput
}
