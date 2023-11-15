package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.server.ClientsInputProvider
import com.mgtriffid.games.cotta.server.ServerSimulationInputProvider
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerSimulationInputProviderImpl @Inject constructor(
    private val clientsInputProvider: ClientsInputProvider,
    private val nonPlayerInputProvider: NonPlayerInputProvider,
    @Named("simulation") private val state: CottaState,
    private val simulationInputHolder: SimulationInputHolder
): ServerSimulationInputProvider {
    override fun prepare() {
        val clientsInput = clientsInputProvider.getInput()

        val nonPlayerEntitiesInput = nonPlayerInputProvider.input(state.entities())

        val inputs = clientsInput.input + nonPlayerEntitiesInput

        logger.trace { "Incoming input has ${inputs.size} entries" }

        inputs.forEach { (eId, components) ->
            logger.trace { "Inputs for entity $eId:" }
            components.forEach { logger.trace { it } }
        }

        simulationInputHolder.set(object: SimulationInput {
            // TODO protect against malicious client sending input for entity not belonging to them
            override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> {
                return inputs
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return clientsInput.playersSawTicks
            }
        })
    }
}
