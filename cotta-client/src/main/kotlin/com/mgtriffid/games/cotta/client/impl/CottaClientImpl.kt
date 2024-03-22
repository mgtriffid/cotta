package com.mgtriffid.games.cotta.client.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.client.*
import com.mgtriffid.games.cotta.client.invokers.impl.PredictedCreatedEntitiesRegistry
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.utils.now
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass

const val STATE_WAITING_THRESHOLD = 5000L

private val logger = KotlinLogging.logger {}

// TODO bloated constructor
class CottaClientImpl @Inject constructor(
    private val game: CottaGame,
    private val network: NetworkClient,
    private val playerInputs: LocalPlayerInputs,
    private val simulations: Simulations,
    private val predictionSimulation: PredictionSimulation,
    private val tickProvider: TickProvider,
    private val predictedCreatedEntitiesRegistry: PredictedCreatedEntitiesRegistry,
    override val localPlayer: LocalPlayer,
    @Named("simulation") private val state: CottaState,
    private val drawableStateProvider: DrawableStateProvider
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
                    network.connect()
                    clientState = ClientState.AwaitingGameState(since = now())
                }

                is ClientState.AwaitingGameState -> {
                    network.fetch()
                    when (val authoritativeState = network.tryGetAuthoritativeState()) {
                        is AuthoritativeState.Ready -> {
                            authoritativeState.apply(state, tickProvider)
                            clientState = ClientState.Running(getCurrentTick())
                        }

                        AuthoritativeState.NotReady -> {
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
                    when (val delta = network.tryGetDelta(getCurrentTick())) {
                        is Delta.Present -> {
                            logger.debug { "Delta present" }
                            integrate(delta)
                            clientState = ClientState.Running(it.currentTick + 1)
                        }

                        Delta.Absent -> {
                            logger.debug { "Delta absent" }
                            // for now do nothing, later we'll guess and keep track of how
                            // long ago did we have a state that is trusted
                        }
                    }
                }
            }
        }
    }

    override fun getDrawableState(alpha: Float, vararg components: KClass<out Component<*>>): DrawableState {
        return drawableStateProvider.get(alpha, components)
    }

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }

    private fun integrate(delta: Delta.Present) {
        logger.debug { "Integrating" }
        processLocalInput()

        simulations.simulate(delta)

        sendDataToServer()
        drawableStateProvider.lastMyInputProcessedByServerSimulation = getLastConfirmedTick(delta)
    }

    private fun getLastConfirmedTick(delta: Delta.Present) =
        delta.input.playersSawTicks()[localPlayer.playerId] ?: 0L

    private fun sendDataToServer() {
        // since this method is called after advancing tick, we need to send inputs for the previous tick
        val createdEntities = predictedCreatedEntitiesRegistry.latest()
        network.send(createdEntities, getCurrentTick())
        network.send(playerInputs.get(getCurrentTick() - 1), getCurrentTick() - 1)
    }

    // called before advancing tick
    private fun processLocalInput() {
        logger.debug { "Processing input" }
        val playerId = localPlayer.playerId
        val localEntities = getEntitiesOwnedByPlayer(playerId)
        logger.debug { "Found ${localEntities.size} entities owned by player $playerId" }
        val localEntitiesWithInputComponents = localEntities.filter {
            it.hasInputComponents()
        }
        logger.debug { "Found ${localEntitiesWithInputComponents.size} entities with input components" }
        playerInputs.collect()
    }

    // GROOM rename
    private fun getEntitiesOwnedByPlayer(playerId: PlayerId) =
        state.entities(atTick = getCurrentTick()).dynamic().filter {
            it.ownedBy == Entity.OwnedBy.Player(playerId)
        } + predictionSimulation.getLocalPredictedEntities()

    sealed class ClientState {
        data object Initial : ClientState()
        class AwaitingGameState(val since: Long) : ClientState()
        data object Disconnected : ClientState()
        class Running(val currentTick: Long) : ClientState() // not sure again
    }
}
