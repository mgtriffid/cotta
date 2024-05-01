package com.mgtriffid.games.cotta.core.simulation

import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

interface Simulation {
    fun tick(input: SimulationInput)
    fun registerSystem(system: CottaSystem)
}
