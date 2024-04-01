package com.mgtriffid.games.cotta.core.simulation

import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

interface AuthoritativeSimulation {
    fun tick(input: SimulationInput)
    fun <T : CottaSystem> registerSystem(systemClass: KClass<T>)
}
