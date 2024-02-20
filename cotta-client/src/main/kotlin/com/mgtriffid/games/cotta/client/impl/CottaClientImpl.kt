package com.mgtriffid.games.cotta.client.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.client.*
import com.mgtriffid.games.cotta.client.interpolation.Interpolators
import com.mgtriffid.games.cotta.client.invokers.impl.PredictedCreatedEntitiesRegistry
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.input.ClientInput
import com.mgtriffid.games.cotta.core.input.impl.ClientInputImpl
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.getComponentClasses
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.ServerCreatedEntitiesRegistry
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.utils.now
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

const val STATE_WAITING_THRESHOLD = 5000L

private val logger = KotlinLogging.logger {}

// TODO bloated constructor
class CottaClientImpl @Inject constructor(
    private val game: CottaGame,
    private val network: NetworkClient,
    private val localInput: CottaClientInput,
    private val clientInputs: ClientInputs,
    private val clientSimulation: ClientSimulation,
    private val predictionSimulation: PredictionSimulation,
    private val tickProvider: TickProvider,
    private val predictedCreatedEntitiesRegistry: PredictedCreatedEntitiesRegistry,
    private val authoritativeToPredictedEntityIdMappings: AuthoritativeToPredictedEntityIdMappings,
    private val serverCreatedEntitiesRegistry: ServerCreatedEntitiesRegistry,
    override val localPlayer: LocalPlayer,
    private val componentRegistry: ComponentRegistry,
    private val interpolators: Interpolators,
    @Named("simulation") private val state: CottaState,
    private val drawableStateProvider: DrawableStateProvider
) : CottaClient {
    private var clientState: ClientState = ClientState.Initial

    override fun initialize() {
        registerComponents()

        registerSystems()
        game.initializeStaticState(state.entities(getCurrentTick()))
        state.setBlank(state.entities(getCurrentTick()))
    }

    private fun registerComponents() {
        getComponentClasses(game).forEach{ kClass ->
            interpolators.register(kClass)
        }
        com.mgtriffid.games.cotta.core.registry.registerComponents(game, componentRegistry)
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

        serverCreatedEntitiesRegistry.data = delta.tracesOfCreatedEntities.toMutableList()
        fillEntityIdMappings(delta)
        remapPredictedCreatedEntityTraces()
        // tick is advanced inside;
        clientSimulation.tick(delta.input)
        delta.applyDiff(state.entities(getCurrentTick())) // unnecessary for deterministic simulation
        val lastMyInputProcessedByServerSimulation = delta.input.playersSawTicks()[localPlayer.playerId] ?: 0L
        drawableStateProvider.lastMyInputProcessedByServerSimulation = lastMyInputProcessedByServerSimulation
        predict(lastMyInputProcessedByServerSimulation)
        sendDataToServer()
    }

    private fun fillEntityIdMappings(delta: Delta.Present) {
        delta.authoritativeToPredictedEntities.forEach { (authoritativeId, predictedId) ->
            logger.debug { "Recording mapping $authoritativeId to $predictedId" }
            authoritativeToPredictedEntityIdMappings[authoritativeId] = predictedId
        }
    }

    private fun remapPredictedCreatedEntityTraces() {
        // TODO analyze performance and optimize
        predictedCreatedEntitiesRegistry.useAuthoritativeEntitiesWherePossible(authoritativeToPredictedEntityIdMappings.all())
    }

    private fun sendDataToServer() {
        // since this method is called after advancing tick, we need to send inputs for the previous tick
        val inputs = clientInputs.get(tickProvider.tick - 1)
        val createdEntities = predictedCreatedEntitiesRegistry.find(tickProvider.tick)
        network.send(inputs, getCurrentTick() - 1)
        network.send(createdEntities, getCurrentTick())
    }

    private fun predict(lastMyInputProcessedByServerSimulation: Long) {
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
        drawableStateProvider.lastMyInputProcessedByServerSimulation = lastMyInputProcessedByServerSimulation
        val unprocessedTicks = clientInputs.all().keys.filter { it > lastMyInputProcessedByServerSimulation }
            .also { logger.debug { it.joinToString() } } // TODO explicit sorting
        logger.debug { "Setting initial predictions state with tick ${getCurrentTick()}" }
        predictionSimulation.predict(state.entities(currentTick), unprocessedTicks, currentTick)
    }

    // called before advancing tick
    private fun processLocalInput() {
        logger.debug { "Processing input" }
        val metaEntity = metaEntity()
        if (metaEntity == null) {
            logger.debug { "No meta entity, returning" }
            return
        }
        val inputs = gatherLocalInput()
        clientInputs.store(inputs)
    }

    private fun gatherLocalInput(): ClientInput {
        val playerId = localPlayer.playerId
        val localEntities = getEntitiesOwnedByPlayer(playerId)
        logger.debug { "Found ${localEntities.size} entities owned by player $playerId" }
        val localEntitiesWithInputComponents = localEntities.filter {
            it.hasInputComponents()
        }
        logger.debug { "Found ${localEntitiesWithInputComponents.size} entities with input components" }
        val inputs = localEntitiesWithInputComponents.distinctBy { it.id }.associate { entity ->
            logger.debug { "Retrieving input for entity '${entity.id}'" }
            entity.id to getInputs(entity)
        }
        return ClientInputImpl(inputs)
    }

    private fun getInputs(entity: Entity) = entity.inputComponents().map { clazz ->
        logger.debug { "Retrieving input of class '${clazz.simpleName}' for entity '${entity.id}'" }
        localInput.input(entity, clazz)
    }

    // GROOM rename
    private fun getEntitiesOwnedByPlayer(playerId: PlayerId) =
        state.entities(atTick = getCurrentTick()).dynamic().filter {
            it.ownedBy == Entity.OwnedBy.Player(playerId)
        } + predictionSimulation.getLocalPredictedEntities()

    private fun metaEntity(): Entity? {
        logger.debug { "Looking for meta entity, metaEntityId = ${localPlayer.metaEntityId}" }
        val entity = state.entities(atTick = getCurrentTick()).all().find {
            it.id == localPlayer.metaEntityId
        }
        if (entity != null) {
            logger.debug { "Meta entity found" }
        } else {
            logger.debug { "Meta entity not found" }
        }
        return entity
    }

    private fun registerSystems() {
        game.serverSystems.forEach { system ->
            clientSimulation.registerSystem(system as KClass<CottaSystem>)
            if (isPredicted(system)) {
                predictionSimulation.registerSystem(system)
            }
        }
    }

    private fun isPredicted(system: KClass<CottaSystem>): Boolean {
        return system.hasAnnotation<Predicted>()
    }

    sealed class ClientState {
        data object Initial : ClientState()
        class AwaitingGameState(val since: Long) : ClientState()
        data object Disconnected : ClientState()
        class Running(val currentTick: Long) : ClientState() // not sure again
    }
}
