package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class ClientIncomingDataBuffer<
    SR : StateRecipe,
    DR : DeltaRecipe
    > {
    val states = TreeMap<Long, AuthoritativeStateData<SR, DR>>()
    val simulationInputs = TreeMap<Long, SimulationInputData>()

    fun storeState(tick: Long, s: AuthoritativeStateData<SR, DR>) {
        states[tick] = s
        cleanUpOldStates(tick)
    }

    fun storeSimulationInput(
        tick: Long,
        simulationInputData: SimulationInputData
    ) {
        simulationInputs[tick] = simulationInputData
        cleanupOldSimulationInputs(tick)
    }

    private fun cleanUpOldStates(tick: Long) {
        cleanUp(states, tick)
    }

    private fun cleanupOldSimulationInputs(tick: Long) {
        cleanUp(simulationInputs, tick)
    }

    // GROOM same on server
    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            logger.debug { "Removing data for tick $it" }
            data.remove(it)
        }
    }
}
