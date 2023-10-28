package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.simulation.SimulationInput

interface ServerSimulationInput {
    fun set(input: SimulationInput)
    fun get(): SimulationInput
}
