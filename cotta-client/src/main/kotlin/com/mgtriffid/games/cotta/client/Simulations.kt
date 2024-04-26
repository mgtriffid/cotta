package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.input.ClientInputId

interface Simulations {
    fun simulate()
    fun getLastConfirmedInput(): ClientInputId
    fun getLastSimulationKind(): SimulationKind
    enum class SimulationKind {
        AUTHORITATIVE,
        GUESSED,
    }
}
