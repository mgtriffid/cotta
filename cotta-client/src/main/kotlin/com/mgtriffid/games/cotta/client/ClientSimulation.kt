package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

interface ClientSimulation {
    fun tick(input: SimulationInput)
    fun <T : CottaSystem> registerSystem(systemClass: KClass<T>)
}
