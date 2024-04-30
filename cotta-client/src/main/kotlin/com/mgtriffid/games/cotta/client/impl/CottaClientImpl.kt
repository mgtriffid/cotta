package com.mgtriffid.games.cotta.client.impl

import com.codahale.metrics.MetricRegistry
import com.google.inject.Inject
import com.mgtriffid.games.cotta.client.*
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.GLOBAL
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.input.ClientInputId
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass

const val STATE_WAITING_THRESHOLD = 500000L

private val logger = KotlinLogging.logger {}

// TODO bloated constructor
class CottaClientImpl @Inject constructor(
    private val game: CottaGame,
    private val network: NetworkClient,
    private val playerInputs: LocalPlayerInputs,
    private val simulations: Simulations,
    @Named(GLOBAL) private val globalTickProvider: TickProvider,
    private val predictionSimulation: PredictionSimulation,
    @Named(SIMULATION) private val simulationTickProvider: TickProvider,
    override val localPlayer: LocalPlayer,
    @Named("simulation") private val state: CottaState,
    @Named("guessed") private val guessedState: CottaState,
    private val drawableStateProvider: DrawableStateProvider,
    private val incomingDataBufferMonitor: IncomingDataBufferMonitor,
    override val debugMetrics: MetricRegistry,
    private val paceRegulator: PaceRegulator
) : CottaClient {

    private var clientState: ClientState = ClientState.Initial

    private var nextSimulationTickAt = -1L
    private var nextLocalTickAt = -1L
    private var tickLength: Long = -1

    override fun initialize() {
        game.initializeStaticState(state.entities(getCurrentTick()))
        state.setBlank(state.entities(getCurrentTick()))
        tickLength = game.config.tickLength
    }

    override fun update(now: Long): UpdateResult {
        tick(now)

        return clientState.let {
            when (it) {
                ClientState.Initial -> UpdateResult.AwaitingGameState
                is ClientState.AwaitingGameState -> UpdateResult.AwaitingGameState
                ClientState.Disconnected -> UpdateResult.Disconnected
                is ClientState.Running -> UpdateResult.Running(
                    InterpolationAlphas(
                        1.0f - (nextSimulationTickAt - now).toFloat() / getSimulationTickLength().toFloat(),
                        1.0f - (nextLocalTickAt - now).toFloat() / getLocalTickLength().toFloat(),
                    )
                )
            }
        }
    }

    private fun getLocalTickLength() =
        paceRegulator.localTickLength(tickLength, debugMetrics)

    private fun getSimulationTickLength(): Long {
        return paceRegulator.simulationTickLength(tickLength, debugMetrics)
    }

    // Should I ask for the state more frequently than at the game's refresh rate?
    private fun tick(now: Long) {
        logger.debug { "Tick at $now" }
        clientState.let {
            when (it) {
                ClientState.Initial -> {
                    network.initialize()
                    network.enterGame()
                    clientState = ClientState.AwaitingGameState(since = now)
                }

                is ClientState.AwaitingGameState -> {
                    network.fetch()
                    when (val authoritativeState =
                        network.tryGetAuthoritativeState()) {
                        is AuthoritativeState.Ready -> {
                            authoritativeState.apply(
                                state,
                                simulationTickProvider,
                                globalTickProvider
                            )
                            clientState = ClientState.Running(getCurrentTick())
                            nextSimulationTickAt = now
                            nextLocalTickAt = now
                        }

                        AuthoritativeState.NotReady -> {
                            network.enterGame()
                            if (now - it.since > STATE_WAITING_THRESHOLD) {
                                disconnect()
                            }
                        }
                    }
                }

                is ClientState.Running -> {
                    network.fetch()
                    if (simulations.hopeless()) {
                        disconnect()
                    } else {
                        run(now)
                        clientState = ClientState.Running(it.currentTick + 1)
                    }
                }

                is ClientState.Disconnected -> {
                    // do nothing
                }
            }
        }
    }

    private fun disconnect() {
        clientState = ClientState.Disconnected
        network.disconnect()
    }

    private fun run(now: Long) {
        logger.info { "nextTickAt is $nextSimulationTickAt, now is $now, difference is ${now - nextSimulationTickAt}" }
        logger.info { "Global tick is ${globalTickProvider.tick}, simulation tick is ${simulationTickProvider.tick}" }
        measureBuffer()
        if (nextSimulationTickAt <= now) {
            logger.info { "run called" }
            integrate()
            nextSimulationTickAt += paceRegulator.simulationTickLength(
                tickLength,
                debugMetrics
            )
        }
        if (nextLocalTickAt <= now) {
            logger.info { "local tick called" }
            processLocalInput()
            predict(
                simulations.getLastConfirmedInput(),
                simulations.getLastSimulationKind()
            )
            nextLocalTickAt += paceRegulator.localTickLength(
                tickLength,
                debugMetrics
            )
            sendDataToServer()
        }
    }

    private fun predict(
        lastConfirmedInput: ClientInputId,
        takeStateFrom: Simulations.SimulationKind
    ) {
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
//        logger.debug { "Setting initial predictions state with tick $currentTick" }
        predictionSimulation.predict(
            when (takeStateFrom) {
                Simulations.SimulationKind.AUTHORITATIVE -> state
                Simulations.SimulationKind.GUESSED -> guessedState
            }.entities(currentTick),
            lastConfirmedInput,
            currentTick
        )
    }

    override fun getDrawableState(
        alphas: InterpolationAlphas,
        vararg components: KClass<out Component<*>>
    ): DrawableState {
        return drawableStateProvider.get(alphas, components)
    }

    private fun getCurrentTick(): Long {
        return globalTickProvider.tick
    }

    private fun integrate() {
        logger.debug { "Integrating" }
        simulations.simulate()

    }

    private fun measureBuffer() {
        incomingDataBufferMonitor.measure()
    }

    private fun sendDataToServer() {
        // since this method is called after advancing tick, we need to send inputs for the previous tick
        playerInputs.unsent().forEach {
            network.send(it.first, it.second, it.third)
        }
    }

    // called before advancing tick
    private fun processLocalInput() {
        logger.debug { "Processing input" }
        playerInputs.collect()
    }

    sealed class ClientState {
        data object Initial : ClientState()
        class AwaitingGameState(val since: Long) : ClientState()
        data object Disconnected : ClientState()
        class Running(val currentTick: Long) : ClientState() // not sure again
    }
}
