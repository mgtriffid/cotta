package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientSimulationInputProvider
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.MapsInputSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.dto.MapsInputRecipeDto
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsInputRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsStateRecipe
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreatedEntities
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.ServerCreatedEntitiesRegistry
import jakarta.inject.Inject
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ClientSimulationInputProviderImpl @Inject constructor(
    private val inputSnapper: InputSnapper<MapsInputRecipe>,
    private val incomingDataBuffer: IncomingDataBuffer<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe>,
    private val tickProvider: TickProvider,
    private val createdEntitiesRegistry: ServerCreatedEntitiesRegistry,
    private val simulationInputHolder: SimulationInputHolder
) : ClientSimulationInputProvider {
    override fun prepare() {
        logger.debug { "Unpacking input for tick ${tickProvider.tick}, incomingDataBuffer is ${incomingDataBuffer.hashCode()}" }
        val inputs = inputSnapper.unpackInputRecipe(incomingDataBuffer.inputs[tickProvider.tick]!!)
        createdEntitiesRegistry.data = incomingDataBuffer.createdEntities[tickProvider.tick + 1]!!
        val simulationInput = object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> {
                return inputs
            }
            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap() // TODO
            }
        }
        simulationInputHolder.set(simulationInput)
    }
}