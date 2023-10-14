package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.server.ClientsInputProvider
import com.mgtriffid.games.cotta.server.ServerSimulationInput
import jakarta.inject.Inject
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerSimulationInputImpl @Inject constructor(
    private val clientsInputProvider: ClientsInputProvider,
    private val nonPlayerInputProvider: NonPlayerInputProvider,
    private val state: CottaState
): ServerSimulationInput {
    override fun prepare(): SimulationInput {
        val clientsInput = clientsInputProvider.getInput()
        val clientsOwnedEntitiesInput = clientsInput

        val nonPlayerEntitiesInput = nonPlayerInputProvider.input(state.entities())

        val inputs = clientsOwnedEntitiesInput.input + nonPlayerEntitiesInput

        logger.trace { "Incoming input has ${inputs.size} entries" }

        inputs.forEach { (eId, components) ->
            logger.trace { "Inputs for entity $eId:" }
            components.forEach { logger.trace { it } }
        }

        return object: SimulationInput {
            // TODO protect against malicious client sending input for entity not belonging to them
            override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> {
                return inputs
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return clientsInput.playersSawTicks
            }
        }
    }
}
