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
import com.mgtriffid.games.cotta.utils.now
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

    private var nextTickAt = now()
    private var tickLength: Long = -1

    override fun initialize() {
        game.initializeStaticState(state.entities(getCurrentTick()))
        state.setBlank(state.entities(getCurrentTick()))
        tickLength = game.config.tickLength
    }

    override fun update(): UpdateResult {
            tick()

        return clientState.let { when (it) {
            ClientState.Initial -> UpdateResult.AwaitingGameState
            is ClientState.AwaitingGameState -> UpdateResult.AwaitingGameState
            ClientState.Disconnected -> UpdateResult.Disconnected
            is ClientState.Running -> UpdateResult.Running(
                1.0f - (nextTickAt - now()).toFloat() / getClientTickLength().toFloat()
            )
        } }

    }

    private fun getClientTickLength() =
        paceRegulator.calculate(tickLength, debugMetrics)

    // Should I ask for the state more frequently than at the game's refresh rate?
    private fun tick() {
        clientState.let {
            when (it) {
                ClientState.Initial -> {
                    network.initialize()
                    network.enterGame()
                    clientState = ClientState.AwaitingGameState(since = now())
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
                            nextTickAt = now()
                        }

                        AuthoritativeState.NotReady -> {
                            network.enterGame()
                            if (now() - it.since > STATE_WAITING_THRESHOLD) {
                                clientState = ClientState.Disconnected
                            }
                        }
                    }
                }

                ClientState.Disconnected -> {
                    // TODO
                }

                is ClientState.Running -> {
                    network.fetch()
                    run()
                    clientState = ClientState.Running(it.currentTick + 1)
                }
            }
        }
    }

    private fun run() {
        val now = now()
        logger.info { "nextTickAt is $nextTickAt, now is $now, difference is ${now - nextTickAt}" }
        logger.info { "Global tick is ${globalTickProvider.tick}, simulation tick is ${simulationTickProvider.tick}" }
        if (nextTickAt <= now) {
            logger.info { "run called" }
            measureBuffer()
            integrate()
            predict(simulations.getLastConfirmedInput(), simulations.getLastSimulationKind())
            nextTickAt += getClientTickLength()
        }
    }

    private fun predict(lastConfirmedInput: ClientInputId, takeStateFrom: SimulationsImpl.SimulationKind) {
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
//        logger.debug { "Setting initial predictions state with tick $currentTick" }
        predictionSimulation.predict(
            when (takeStateFrom) {
                SimulationsImpl.SimulationKind.AUTHORITATIVE -> state
                SimulationsImpl.SimulationKind.GUESSED -> guessedState
            }.entities(currentTick),
            lastConfirmedInput,
            currentTick
        )
    }

    override fun getDrawableState(
        alpha: Float,
        vararg components: KClass<out Component<*>>
    ): DrawableState {
        return drawableStateProvider.get(alpha, components)
    }

    private fun getCurrentTick(): Long {
        return globalTickProvider.tick
    }

    private fun integrate() {
        logger.debug { "Integrating" }
        processLocalInput()
        simulations.simulate()

        sendDataToServer()
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
