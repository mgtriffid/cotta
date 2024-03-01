package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeSimulation
import com.mgtriffid.games.cotta.client.AuthoritativeToPredictedEntityIdMappings
import com.mgtriffid.games.cotta.client.ClientInputs
import com.mgtriffid.games.cotta.client.GuessedSimulation
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.client.SimulationDirector
import com.mgtriffid.games.cotta.client.Simulations
import com.mgtriffid.games.cotta.client.invokers.impl.PredictedCreatedEntitiesRegistry
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.ServerCreatedEntitiesRegistry
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SimulationsImpl @Inject constructor(
    private val game: CottaGame,
    private val serverCreatedEntitiesRegistry: ServerCreatedEntitiesRegistry,
    private val simulation: AuthoritativeSimulation,
    private val guessedSimulation: GuessedSimulation,
    private val simulationDirector: SimulationDirector,
    private val localInputs: ClientInputs,
    @Named("simulation") private val state: CottaState,
    private val tickProvider: TickProvider,
    private val predictedCreatedEntitiesRegistry: PredictedCreatedEntitiesRegistry,
    private val localPlayer: LocalPlayer,
    private val authoritativeToPredictedEntityIdMappings: AuthoritativeToPredictedEntityIdMappings,
    private val predictionSimulation: PredictionSimulation,
    ): Simulations {
    override fun simulate(delta: Delta.Present) {
        val instructions = simulationDirector.instruct(tickProvider.tick).also { logger.info { "Instructions: $it" } }
        serverCreatedEntitiesRegistry.data = delta.tracesOfCreatedEntities.toMutableList()
        fillEntityIdMappings(delta)
        remapPredictedCreatedEntityTraces()
        // tick is advanced inside;
        simulation.tick(delta.input)
        processMetaEntitiesDiff(delta)
        val lastConfirmedTick = getLastConfirmedTick(delta)
        predict(lastConfirmedTick)
    }

    private fun fillEntityIdMappings(delta: Delta.Present) {
        delta.authoritativeToPredictedEntities.forEach { (authoritativeId, predictedId) ->
            logger.debug { "Recording mapping $authoritativeId to $predictedId" }
            authoritativeToPredictedEntityIdMappings[authoritativeId] = predictedId
        }
    }

    private fun predict(serverSawOurTick: Long) {
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
        val unprocessedTicks = localInputs.all().keys.filter { it > serverSawOurTick }
            .also { logger.info { it.joinToString() } } // TODO explicit sorting
        logger.debug { "Setting initial predictions state with tick ${getCurrentTick()}" }
        predictionSimulation.predict(state.entities(currentTick), unprocessedTicks, currentTick)
    }

    private fun remapPredictedCreatedEntityTraces() {
        // TODO analyze performance and optimize
        predictedCreatedEntitiesRegistry.useAuthoritativeEntitiesWherePossible(authoritativeToPredictedEntityIdMappings.all())
    }

    private fun processMetaEntitiesDiff(delta: Delta.Present) {
        delta.metaEntitiesDiff.forEach { (entityId, playerId) ->
            val newMetaEntity = state.entities(getCurrentTick()).create(entityId, Entity.OwnedBy.Player(playerId))
            game.metaEntitiesInputComponents.forEach { newMetaEntity.addInputComponent(it) }
        }
    }

    private fun getLastConfirmedTick(delta: Delta.Present) =
        delta.input.playersSawTicks()[localPlayer.playerId] ?: 0L

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }
}
