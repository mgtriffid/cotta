package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeSimulation
import com.mgtriffid.games.cotta.client.AuthoritativeToPredictedEntityIdMappings
import com.mgtriffid.games.cotta.client.GuessedSimulation
import com.mgtriffid.games.cotta.client.LocalPlayerInputs
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.client.SimulationDirector
import com.mgtriffid.games.cotta.client.Simulations
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SimulationsImpl @Inject constructor(
    private val game: CottaGame,
    private val simulation: AuthoritativeSimulation,
    private val guessedSimulation: GuessedSimulation,
    private val simulationDirector: SimulationDirector,
    private val playerInputs: LocalPlayerInputs,
    @Named("simulation") private val state: CottaState,
    private val tickProvider: TickProvider,
    private val localPlayer: LocalPlayer,
    private val authoritativeToPredictedEntityIdMappings: AuthoritativeToPredictedEntityIdMappings,
    private val predictionSimulation: PredictionSimulation,
    ): Simulations {
    override fun simulate(delta: Delta.Present) {
        val instructions = simulationDirector.instruct(tickProvider.tick).also { logger.info { "Instructions: $it" } }
        // tick is advanced inside;
        simulation.tick(delta.input)
//        processMetaEntitiesDiff(delta) // TODO maybe playersDiff goes here if even needed
        val lastConfirmedTick = getLastConfirmedTick(delta)
        predict(lastConfirmedTick)
    }

    private fun predict(serverSawOurTick: Long) {
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
        val unprocessedTicks = playerInputs.all().keys.filter { it > serverSawOurTick }
            .also { logger.info { it.joinToString() } } // TODO explicit sorting
        logger.debug { "Setting initial predictions state with tick ${getCurrentTick()}" }
//        predictionSimulation.predict(state.entities(currentTick), unprocessedTicks, currentTick)
    }

    private fun getLastConfirmedTick(delta: Delta.Present) =
        delta.input.playersSawTicks()[localPlayer.playerId] ?: 0L

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }
}
