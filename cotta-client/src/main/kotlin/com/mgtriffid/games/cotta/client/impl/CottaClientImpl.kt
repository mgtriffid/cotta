package com.mgtriffid.games.cotta.client.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.client.*
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.GLOBAL
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.*
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
    @Named(SIMULATION) private val simulationTickProvider: TickProvider,
    override val localPlayer: LocalPlayer,
    @Named("simulation") private val state: CottaState,
    private val drawableStateProvider: DrawableStateProvider,
    private val incomingDataBufferMonitor: IncomingDataBufferMonitor,
) : CottaClient {
    private var clientState: ClientState = ClientState.Initial

    override fun initialize() {
        game.initializeStaticState(state.entities(getCurrentTick()))
        state.setBlank(state.entities(getCurrentTick()))
    }

    override fun tick() {
        logger.debug { "Running ${CottaClientImpl::class.simpleName}" }
        clientState.let {
            when (it) {
                ClientState.Initial -> {
                    network.initialize()
                    network.enterGame()
                    clientState = ClientState.AwaitingGameState(since = now())
                }

                is ClientState.AwaitingGameState -> {
                    network.fetch()
                    when (val authoritativeState = network.tryGetAuthoritativeState()) {
                        is AuthoritativeState.Ready -> {
                            authoritativeState.apply(state, simulationTickProvider, globalTickProvider)
                            clientState = ClientState.Running(getCurrentTick())
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
                    measureBuffer()
                    integrate()
                    clientState = ClientState.Running(it.currentTick + 1)
                }
            }
        }
    }

    override fun getDrawableState(alpha: Float, vararg components: KClass<out Component<*>>): DrawableState {
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
        network.send(playerInputs.get(getCurrentTick() - 1), getCurrentTick() - 1)
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
