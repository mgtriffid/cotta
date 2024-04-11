package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.simulation.SimulationInput

class ClientSimulationInput(
    val tick: Long, // mb not needed,
    val simulationInput: SimulationInput,
    val idSequence: Int,
    val confirmedClientInput: ClientInputId
)
