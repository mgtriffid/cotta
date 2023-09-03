package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.client.impl.ClientSimulationImpl
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

interface ClientSimulation {
    companion object {
        fun getInstance(
            state: CottaState,
            tickProvider: TickProvider,
            historyLength: Int
        ): ClientSimulation = ClientSimulationImpl(
            state,
            tickProvider,
            historyLength
        )
    }

    fun setInputForUpcomingTick(input: SimulationInput)
    fun tick()
    fun <T : CottaSystem> registerSystem(systemClass: KClass<T>)
}
