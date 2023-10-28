package com.mgtriffid.games.cotta.core.simulation

import com.mgtriffid.games.cotta.core.simulation.SimulationInput

interface SimulationInputHolder {
    fun set(input: SimulationInput)
    fun get(): SimulationInput
}
