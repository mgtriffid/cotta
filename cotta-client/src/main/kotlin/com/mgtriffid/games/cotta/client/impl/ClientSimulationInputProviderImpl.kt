package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeToPredictedEntityIdMappings
import com.mgtriffid.games.cotta.client.ClientSimulationInputProvider
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsInputRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsStateRecipe
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.ServerCreatedEntitiesRegistry
import jakarta.inject.Inject
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ClientSimulationInputProviderImpl @Inject constructor(
    private val stateSnapper: StateSnapper<MapsStateRecipe, MapsDeltaRecipe>,
    private val incomingDataBuffer: IncomingDataBuffer<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe>,
    private val tickProvider: TickProvider,
    private val createdEntitiesRegistry: ServerCreatedEntitiesRegistry,
    private val authoritativeToPredictedEntityIdMappings: AuthoritativeToPredictedEntityIdMappings
) : ClientSimulationInputProvider {
    override fun prepare() {
        logger.info { "Unpacking input for tick ${tickProvider.tick}" }
        createdEntitiesRegistry.data = incomingDataBuffer.createdEntities[tickProvider.tick + 1]!!.traces.map { (traceRecipe, entityId) ->
            Pair(stateSnapper.unpackTrace(traceRecipe), entityId)
        }.toMutableList()
        incomingDataBuffer.createdEntities[tickProvider.tick + 1]!!.mappedPredictedIds.forEach { (authoritativeId, predictedId) ->
            authoritativeToPredictedEntityIdMappings[authoritativeId] = predictedId
        }
    }
}