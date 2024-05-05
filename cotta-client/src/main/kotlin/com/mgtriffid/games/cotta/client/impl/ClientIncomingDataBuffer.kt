package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.utils.RingBuffer
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class ClientIncomingDataBuffer<
    SR : StateRecipe,
    DR : DeltaRecipe
    > {
    val states = RingBuffer<AuthoritativeStateData<SR, DR>>(128)
    val simulationInputs = RingBuffer<SimulationInputData>(128)

    fun storeState(tick: Long, s: AuthoritativeStateData<SR, DR>) {
        states[tick] = s
    }

    fun storeSimulationInput(
        tick: Long,
        simulationInputData: SimulationInputData
    ) {
        simulationInputs[tick] = simulationInputData
    }
}
