package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.server.ClientsInputProvider
import com.mgtriffid.games.cotta.server.EntitiesCreatedOnClientsRegistry
import com.mgtriffid.games.cotta.server.PredictedToAuthoritativeIdMappings
import com.mgtriffid.games.cotta.server.ServerSimulationInputProvider
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerSimulationInputProviderImpl @Inject constructor(
    private val clientsInputProvider: ClientsInputProvider,
    private val nonPlayerInputProvider: NonPlayerInputProvider,
    @Named("simulation") private val state: CottaState,
    private val simulationInputHolder: SimulationInputHolder,
    private val entitiesCreatedOnClientsRegistry: EntitiesCreatedOnClientsRegistry,
    private val predictedToAuthoritativeIdMappings: PredictedToAuthoritativeIdMappings,
    private val tickProvider: TickProvider
): ServerSimulationInputProvider {
    override fun prepare() {
        val dataFromClients = clientsInputProvider.getInput()
        val clientsInput = dataFromClients.first
        val predictedClientEntities = dataFromClients.second
        entitiesCreatedOnClientsRegistry.populate(predictedClientEntities)

        val nonPlayerEntitiesInput = nonPlayerInputProvider.input(state.entities(tickProvider.tick))

        val remappedInput = clientsInput.input.entries.associate { (eId, components) ->
            when (eId) {
                is PredictedEntityId -> {
                    val authoritativeId = predictedToAuthoritativeIdMappings[eId]
                    if (authoritativeId != null) {
                        logger.trace { "Remapping input for entity $eId to $authoritativeId" }
                        authoritativeId to components
                    } else {
                        // TODO probably warning or not needed in the resulting map at all
                        logger.trace { "Not remapping input for entity $eId" }
                        eId to components
                    }
                }
                is AuthoritativeEntityId -> {
                    logger.trace { "Not remapping input for entity $eId" }
                    eId to components
                }
            }
        }

        val inputs = remappedInput + nonPlayerEntitiesInput

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
//        playersSawTicks.set(clientsInput.playersSawTicks)
    }

    override fun get(): SimulationInput {
        return simulationInputHolder.get()
    }
}
