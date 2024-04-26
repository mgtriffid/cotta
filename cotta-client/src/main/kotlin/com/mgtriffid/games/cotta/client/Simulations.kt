package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.client.impl.Delta
import com.mgtriffid.games.cotta.client.impl.SimulationsImpl
import com.mgtriffid.games.cotta.core.input.ClientInputId

interface Simulations {
    fun simulate()
    fun getLastConfirmedInput(): ClientInputId
    fun getLastSimulationKind(): SimulationsImpl.SimulationKind
}
