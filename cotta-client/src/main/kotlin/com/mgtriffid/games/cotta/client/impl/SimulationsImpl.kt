package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeSimulation
import com.mgtriffid.games.cotta.client.ClientPlayers
import com.mgtriffid.games.cotta.client.GuessedSimulation
import com.mgtriffid.games.cotta.client.LocalPlayerInputs
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.client.SimulationDirector
import com.mgtriffid.games.cotta.client.Simulations
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.GLOBAL
import com.mgtriffid.games.cotta.core.SIMULATION
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
    @Named(GLOBAL) private val tickProvider: TickProvider,
    private val localPlayer: LocalPlayer,
    private val players: ClientPlayers,
    private val predictionSimulation: PredictionSimulation,
    ): Simulations {
    override fun simulate(delta: Delta.Present) {
        val instructions = simulationDirector.instruct(tickProvider.tick).also { logger.info { "Instructions: $it" } }
        // Now we need to consider the situation when we have guessed simulation.
        // Suddenly it breaks tickProvider. Right now we have only one, and it
        // is shared between simulation and, let's say, Global Orchestration.
        // Ideally simulation tick is one thing, guessed simulation tick - another.
        // And so is Predicted simulation tick. And finally the global one.
        // Looks like what we have currently is exactly Global tick: it is responsible
        // for sending inputs properly, for fetching necessary data, etc.
        // tick is advanced inside;
        tickProvider.tick++
        simulation.tick(delta.input)
        processPlayersDiff(delta)
        val lastConfirmedTick = getLastConfirmedTick(delta)
        predict(lastConfirmedTick)
    }

    private fun processPlayersDiff(delta: Delta.Present) {
        delta.playersDiff.forEach(players::add)
    }

    private fun predict(serverSawOurTick: Long) {
        logger.info { "Predicting" }
        val currentTick = getCurrentTick()
        val unprocessedTicks = playerInputs.all().keys.filter { it > serverSawOurTick }
            .also { logger.info { it.joinToString() } } // TODO explicit sorting
        logger.info { "Setting initial predictions state with tick ${getCurrentTick()}" }
        predictionSimulation.predict(state.entities(currentTick), unprocessedTicks, currentTick)
    }

    private fun getLastConfirmedTick(delta: Delta.Present) =
        delta.input.playersSawTicks()[localPlayer.playerId] ?: 0L

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }
}
