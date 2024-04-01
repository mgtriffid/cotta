package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.simulation.AuthoritativeSimulation
import com.mgtriffid.games.cotta.client.GuessedSimulation
import com.mgtriffid.games.cotta.client.Instruction
import com.mgtriffid.games.cotta.client.LastClientTickProcessedByServer
import com.mgtriffid.games.cotta.client.LocalPlayerInputs
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.client.SimulationDirector
import com.mgtriffid.games.cotta.client.Simulations
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.GLOBAL
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
    private val deltas: Deltas,
    @Named("simulation") private val state: CottaState,
    @Named(GLOBAL) private val tickProvider: TickProvider,
    private val localPlayer: LocalPlayer,
    private val predictionSimulation: PredictionSimulation,
    private val lastClientTickProcessedByServer: LastClientTickProcessedByServer
): Simulations {
    override fun simulate() {
        val instructions = simulationDirector.instruct(tickProvider.tick).also { logger.debug { "Instructions: $it" } }
        // Now we need to consider the situation when we have guessed simulation.
        // Suddenly it breaks tickProvider. Right now we have only one, and it
        // is shared between simulation and, let's say, Global Orchestration.
        // Ideally simulation tick is one thing, guessed simulation tick - another.
        // And so is Predicted simulation tick. And finally the global one.
        // Looks like what we have currently is exactly Global tick: it is responsible
        // for sending inputs properly, for fetching necessary data, etc.
        // tick is advanced inside;
        tickProvider.tick++
        var lastConfirmedTick = 0L
        instructions.forEachIndexed { index, it ->
            when (it) {
                is Instruction.IntegrateAuthoritative -> {
                    val delta = deltas.get(it.tick)
                    simulation.tick(delta.input.simulationInput)
                    lastConfirmedTick = getLastConfirmedTick(delta)
                }
                is Instruction.CopyAuthoritativeToGuessed -> {
//                    guessedSimulation.copyFrom(simulation)
                }
                is Instruction.IntegrateGuessed -> {
//                    guessedSimulation.tick(it.tick)
                }
            }
        }
//        simulation.tick(delta.input)
        lastClientTickProcessedByServer.tick = lastConfirmedTick
        predict(lastConfirmedTick)
    }

    private fun predict(serverSawOurTick: Long) {
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
        val unprocessedTicks = playerInputs.all().keys.filter { it > serverSawOurTick }
            .also { logger.debug { it.joinToString() } } // TODO explicit sorting
        logger.debug { "Setting initial predictions state with tick $currentTick" }
        predictionSimulation.predict(state.entities(currentTick), unprocessedTicks, currentTick)
    }

    private fun getLastConfirmedTick(delta: Delta) =
        delta.input.simulationInput.playersSawTicks()[localPlayer.playerId] ?: 0L

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }
}
