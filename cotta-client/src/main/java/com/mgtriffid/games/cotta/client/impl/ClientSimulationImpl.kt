package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientSimulation
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.SimulationInput

class ClientSimulationImpl(
    val tickProvider: TickProvider,
    val historyLength: Int
) : ClientSimulation {
    private var input: SimulationInput = object : SimulationInput {
        override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> = emptyMap()
    }
    private lateinit var state: CottaState

    override fun setInputForUpcomingTick(input: SimulationInput) {
        this.input = input
    }

    override fun setState(state: CottaState) {
        this.state = state
    }
}
