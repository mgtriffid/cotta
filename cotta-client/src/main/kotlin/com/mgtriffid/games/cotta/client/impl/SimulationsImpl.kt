package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientSimulationInput
import com.mgtriffid.games.cotta.core.simulation.Simulation
import com.mgtriffid.games.cotta.client.GuessedSimulation
import com.mgtriffid.games.cotta.client.Instruction
import com.mgtriffid.games.cotta.client.LastClientTickProcessedByServer
import com.mgtriffid.games.cotta.client.LocalPlayerInputs
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.client.SimulationDirector
import com.mgtriffid.games.cotta.client.Simulations
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.GLOBAL
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.simulation.PlayersDiff
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import jakarta.inject.Inject
import jakarta.inject.Named
import jdk.jfr.Name
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SimulationsImpl @Inject constructor(
    private val game: CottaGame,
    @Named(SIMULATION) private val simulation: Simulation,
    @Named("guessed") private val guessedSimulation: Simulation,
    private val simulationDirector: SimulationDirector,
    private val playerInputs: LocalPlayerInputs,
    private val deltas: Deltas,
    @Named("simulation") private val state: CottaState,
    @Named("guessed") private val guessedState: CottaState,
    @Named(GLOBAL) private val tickProvider: TickProvider,
    private val localPlayer: LocalPlayer,
    private val predictionSimulation: PredictionSimulation,
    private val lastClientTickProcessedByServer: LastClientTickProcessedByServer,
    @Named("simulation") private val authoritativeTickProvider: TickProvider,
    @Named("guessed") private val guessedTickProvider: TickProvider
) : Simulations {
    override fun simulate() {
        val instructions = simulationDirector.instruct(tickProvider.tick)
            .also { logger.debug { "Instructions: $it" } }
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
                    copyAuthoritativeToGuessed()
                }

                is Instruction.IntegrateGuessed -> {
                    val knownDelta = deltas.get(it.tick)
                    val input = knownDelta.input
                    val guessedInput = guessInput(input, tickProvider.tick)
                    guessedSimulation.tick(guessedInput.simulationInput)
                }
            }
        }
//        simulation.tick(delta.input)
        lastClientTickProcessedByServer.tick = lastConfirmedTick
        predict(
            lastConfirmedTick, when (instructions.last()) {
                is Instruction.IntegrateAuthoritative -> SimulationKind.AUTHORITATIVE
                is Instruction.CopyAuthoritativeToGuessed -> throw IllegalStateException(
                    "CopyAuthoritativeToGuessed should not be the last instruction"
                )

                is Instruction.IntegrateGuessed -> SimulationKind.GUESSED
            }
        )
    }

    private fun copyAuthoritativeToGuessed() {
        state.copyTo(guessedState)
        guessedTickProvider.tick = authoritativeTickProvider.tick
    }

    private fun guessInput(
        input: ClientSimulationInput,
        tick: Long
    ): ClientSimulationInput {
        val playersSawTicks =
            input.simulationInput.playersSawTicks().mapValues { (_, t) ->
                tick - input.tick + t
            }
        return ClientSimulationInput(
            tick = input.tick,
            simulationInput = object : SimulationInput {
                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return input.simulationInput.inputForPlayers()
                }

                override fun nonPlayerInput(): NonPlayerInput {
                    return NonPlayerInput.Blank
                }

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return playersSawTicks
                }

                override fun playersDiff() = PlayersDiff.Empty
            },
            idSequence = input.idSequence
        )
    }

    private enum class SimulationKind {
        AUTHORITATIVE,
        GUESSED,
    }

    private fun predict(serverSawOurTick: Long, takeStateFrom: SimulationKind) {
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
        val unprocessedTicks =
            playerInputs.all().keys.filter { it > serverSawOurTick }
                .also { logger.debug { it.joinToString() } } // TODO explicit sorting
        logger.debug { "Setting initial predictions state with tick $currentTick" }
        predictionSimulation.predict(
            when (takeStateFrom) {
                SimulationKind.AUTHORITATIVE -> state
                SimulationKind.GUESSED -> guessedState
            }.entities(currentTick),
            unprocessedTicks,
            currentTick
        )
    }

    private fun getLastConfirmedTick(delta: Delta) =
        delta.input.simulationInput.playersSawTicks()[localPlayer.playerId]
            ?: 0L

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }
}
