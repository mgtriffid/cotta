package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder

class SimulationInputHolderImpl : SimulationInputHolder {
    private var input: SimulationInput? = null
    override fun set(input: SimulationInput) {
        this.input = input
    }

    override fun get(): SimulationInput {
        return this.input ?: throw IllegalStateException("Input not prepared")
    }
}
