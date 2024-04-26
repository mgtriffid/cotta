package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientSimulationInput
import com.mgtriffid.games.cotta.core.simulation.Simulation
import com.mgtriffid.games.cotta.client.Instruction
import com.mgtriffid.games.cotta.client.SimulationDirector
import com.mgtriffid.games.cotta.client.Simulations
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.GLOBAL
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.simulation.PlayersDiff
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SimulationsImpl @Inject constructor(
    private val game: CottaGame,
    @Named(SIMULATION) private val simulation: Simulation,
    @Named("guessed") private val guessedSimulation: Simulation,
    private val simulationDirector: SimulationDirector,
    private val deltas: Deltas,
    @Named("simulation") private val state: CottaState,
    @Named("guessed") private val guessedState: CottaState,
    @Named(GLOBAL) private val tickProvider: TickProvider,
    @Named("simulation") private val authoritativeTickProvider: TickProvider,
    @Named("guessed") private val guessedTickProvider: TickProvider
) : Simulations {

    private var lastConfirmedInput: ClientInputId? = null

    override fun getLastConfirmedInput(): ClientInputId {
        return lastConfirmedInput ?: throw IllegalStateException("No last confirmed input")
    }

    private lateinit var lastSimulationKind: SimulationKind

    override fun getLastSimulationKind(): SimulationKind {
        return lastSimulationKind
    }

    override fun simulate() {
        val instructions = simulationDirector.instruct(tickProvider.tick)
            .also { logger.info { "Instructions: $it" } }
        tickProvider.tick++
//        var lastConfirmedInput: ClientInputId? = null
        instructions.forEach {
            when (it) {
                is Instruction.IntegrateAuthoritative -> {
                    val delta = deltas.get(it.tick)
                    simulation.tick(delta.input.simulationInput)
                    lastConfirmedInput = getLastConfirmedInput(delta)
                }

                is Instruction.CopyAuthoritativeToGuessed -> {
                    copyAuthoritativeToGuessed()
                }

                is Instruction.IntegrateGuessed -> {
                    val knownDelta = deltas.get(it.tick)
                    val input = knownDelta.input
                    val guessedInput = guessInput(input, tickProvider.tick)
                    guessedSimulation.tick(guessedInput.simulationInput)
                    lastConfirmedInput = ClientInputId(getLastConfirmedInput(knownDelta).id + (tickProvider.tick - it.tick).toInt())
                }
            }
        }
        lastSimulationKind = when (instructions.last()) {
            is Instruction.IntegrateAuthoritative -> SimulationKind.AUTHORITATIVE
            is Instruction.CopyAuthoritativeToGuessed -> throw IllegalStateException(
                "CopyAuthoritativeToGuessed should not be the last instruction"
            )

            is Instruction.IntegrateGuessed -> SimulationKind.GUESSED
        }
        lastConfirmedInput = lastConfirmedInput ?: throw IllegalStateException(
            "No way ${Instruction.CopyAuthoritativeToGuessed::class.simpleName} was the only instruction"
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
            idSequence = input.idSequence,
            confirmedClientInput = ClientInputId(input.confirmedClientInput.id + (tick - input.tick).toInt())
        )
    }

    enum class SimulationKind {
        AUTHORITATIVE,
        GUESSED,
    }

    private fun getLastConfirmedInput(delta: Delta) =
        delta.input.confirmedClientInput

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }
}
