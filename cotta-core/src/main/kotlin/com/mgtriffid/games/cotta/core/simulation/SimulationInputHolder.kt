package com.mgtriffid.games.cotta.core.simulation

interface SimulationInputHolder {
    fun set(input: SimulationInput)
    fun get(): SimulationInput
}
