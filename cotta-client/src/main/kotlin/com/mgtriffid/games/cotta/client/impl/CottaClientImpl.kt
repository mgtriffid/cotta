package com.mgtriffid.games.cotta.client.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.client.*
import com.mgtriffid.games.cotta.client.invokers.impl.PredictedCreatedEntitiesRegistry
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.input.ClientInput
import com.mgtriffid.games.cotta.core.input.impl.ClientInputImpl
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.utils.now
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

const val STATE_WAITING_THRESHOLD = 5000L

private val logger = KotlinLogging.logger {}

// TODO bloated constructor
class CottaClientImpl<SR : StateRecipe, DR : DeltaRecipe, IR : InputRecipe> @Inject constructor(
    val game: CottaGame,
    val engine: CottaEngine<SR, DR, IR>, // weird type parameterization
    val network: NetworkClient,
    val localInput: CottaClientInput,
    val clientInputs: ClientInputs,
    val clientSimulation: ClientSimulation,
    private val predictionSimulation: PredictionSimulation,
    val input: ClientSimulationInputProvider,
    override val tickProvider: TickProvider,
    private val predictedCreatedEntitiesRegistry: PredictedCreatedEntitiesRegistry,
    private val localPlayer: LocalPlayer,
    private val incomingDataBuffer: IncomingDataBuffer<SR, DR, IR>,
    @Named("simulation") override val state: CottaState // Todo not expose as public
) : CottaClient {
    private var clientState: ClientState = ClientState.Initial // the only real `var` here
    private val componentsRegistry = engine.getComponentsRegistry()

    override fun initialize() {
        registerComponents()
        registerSystems()
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
                            logger.debug { "Delta present, we will integrate" }
                            integrate(delta)
                            clientState = ClientState.Running(it.currentTick + 1)
                        }

                        Delta.Absent -> {
                            logger.debug { "Delta not present" }
                            // for now do nothing, later we'll guess and keep track of how
                            // long ago did we have a state that is trusted
                        }
                    }
                }
            }
        }
    }

    override fun getPredictedEntities(): List<Entity> {
        return predictionSimulation.getPredictedEntities()
    }

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }

    private fun integrate(delta: Delta.Present) {
        logger.debug { "Integrating" }
        val tick = getCurrentTick()
        logger.debug { "Tick = $tick" }

        fetchInput()
        // tick is advanced inside;
        clientSimulation.tick(delta.input)
        delta.applyDiff(state.entities(tick + 1)) // unnecessary for deterministic simulation
        predict()
        sendDataToServer()
    }

    private fun sendDataToServer() {
        // since this method is called after advancing tick, we need to send inputs for the previous tick
        val inputs = clientInputs.get(tickProvider.tick - 1)
        val createdEntities = predictedCreatedEntitiesRegistry.find(tickProvider.tick)
        network.send(inputs, getCurrentTick() - 1)
        network.send(createdEntities, getCurrentTick())
    }

    private fun predict() {
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
        val lastMyInputProcessedByServerSimulation = getLastInputProcessedByServer(currentTick, localPlayer.playerId)// TODO gracefully handle missing
        val unprocessedTicks = clientInputs.all().keys.filter { it > lastMyInputProcessedByServerSimulation }
            .also { logger.info { it.joinToString() } } // TODO explicit sorting
        logger.info { "Setting initial predictions state with tick ${getCurrentTick()}" }
        predictionSimulation.predict(state.entities(currentTick), unprocessedTicks)
    }

    private fun getLastInputProcessedByServer(currentTick: Long, playerId: PlayerId): Long {
        val lastMyInputProcessedByServerSimulation =
            incomingDataBuffer.playersSawTicks[currentTick - 1]!![playerId]?.let {
                logger.debug { "Got $it as processed input from Server" }
                it
            } ?: 0L.also {
                logger.debug { "Did not find our input in server's input, assuming none of our input was processed yet" }
            }// TODO gracefully handle missing
        return lastMyInputProcessedByServerSimulation
    }

    // called before advancing tick
    private fun fetchInput() {
        processLocalInput()

        input.prepare()
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
        val inputs = localEntitiesWithInputComponents.associate { entity ->
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
        state.entities(atTick = getCurrentTick()).all().filter {
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

    // TODO probably this is wrong place
    private fun registerComponents() {
        game.componentClasses.forEach {
            componentsRegistry.registerComponentClass(it)
        }
        game.inputComponentClasses.forEach {
            componentsRegistry.registerInputComponentClass(it)
        }
        game.effectClasses.forEach { effectClass ->
            componentsRegistry.registerEffectClass(effectClass)
        }
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
        object Initial : ClientState()
        class AwaitingGameState(val since: Long) : ClientState()
        object Disconnected : ClientState()
        class Running(val currentTick: Long) : ClientState() // not sure again
    }
}