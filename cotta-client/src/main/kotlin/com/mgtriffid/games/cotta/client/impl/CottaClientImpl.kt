package com.mgtriffid.games.cotta.client.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.client.*
import com.mgtriffid.games.cotta.client.invokers.impl.PredictedCreatedEntitiesRegistry
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
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.protocol.ClientToServerCreatedPredictedEntitiesDto
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.protocol.KindOfData
import com.mgtriffid.games.cotta.utils.now
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

const val STATE_WAITING_THRESHOLD = 5000L

private val logger = KotlinLogging.logger {}

class CottaClientImpl<SR : StateRecipe, DR : DeltaRecipe, IR : InputRecipe> @Inject constructor(
    val game: CottaGame,
    val engine: CottaEngine<SR, DR, IR>, // weird type parameterization
    val network: CottaClientNetwork,
    val localInput: CottaClientInput,
    val clientInputs: ClientInputs,
    val clientSimulation: ClientSimulation,
    private val predictionSimulation: PredictionSimulation,
    val input: ClientSimulationInputProvider,
    override val tickProvider: TickProvider,
    private val predictedCreatedEntitiesRegistry: PredictedCreatedEntitiesRegistry,
    private val playerIdHolder: PlayerIdHolder,
    private val incomingDataBuffer: IncomingDataBuffer<SR, DR, IR>,
    @Named("simulation") override val state: CottaState // Todo not expose as public
) : CottaClient {
    val lagCompLimit: Int = 8 // TODO move to config and bind properly
    val bufferLength: Int = 3
    private var clientState: ClientState = ClientState.Initial
    private val componentsRegistry = engine.getComponentsRegistry()
    private val stateSnapper = engine.getStateSnapper()
    private val snapsSerialization = engine.getSnapsSerialization()
    private val inputSnapper = engine.getInputSnapper()
    private val inputSerialization = engine.getInputSerialization()
    private var metaEntityId: EntityId? = null

    override fun initialize() {
        registerComponents()
        registerSystems()
    }

    override fun tick() {
        logger.debug { "Running ${CottaClientImpl::class.simpleName}" }
        clientState.let {
            when (it) {
                ClientState.Initial -> {
                    connect()
                    clientState = ClientState.AwaitingGameState(since = System.currentTimeMillis())
                }

                is ClientState.AwaitingGameState -> {
                    fetchData()
                    // TODO ensure that if metaEntity did not come we still can operate
                    if (stateAvailable()) {
                        setStateFromAuthoritative()
                        clientState = ClientState.Running(getCurrentTick())
                    } else {
                        if (now() - it.since > STATE_WAITING_THRESHOLD) {
                            clientState = ClientState.Disconnected
                        }
                    }
                }

                ClientState.Disconnected -> {
                    // TODO
                }

                is ClientState.Running -> {
                    fetchData()
                    if (deltaAvailableForTick(getCurrentTick())) {
                        logger.debug { "Delta available, we will integrate" }
                        integrate()
                        clientState = ClientState.Running(it.currentTick + 1)
                    } else {
                        logger.debug { "Delta not available" }
                        // for now do nothing, later we'll guess and keep track of how
                        // long ago did we have a state that is trusted
                    }
                }
            }
        }
    }

    override fun getPredictedEntities(): List<Entity> {
        return predictionSimulation.getPredictedEntities()
    }

    private fun setStateFromAuthoritative() {
        logger.debug { "Setting state from authoritative" }
        val fullStateTick = incomingDataBuffer.states.lastKey()
        val stateRecipe = incomingDataBuffer.states[fullStateTick]!!
        state.set(fullStateTick, blankEntities())
        tickProvider.tick = fullStateTick
        stateSnapper.unpackStateRecipe(state.entities(atTick = fullStateTick), stateRecipe)
        ((fullStateTick + 1)..(fullStateTick + lagCompLimit)).forEach { tick ->
            state.advance(tick - 1)
            tickProvider.tick++
            stateSnapper.unpackDeltaRecipe(state.entities(atTick = tick), incomingDataBuffer.deltas[tick]!!)
        }
    }

    private fun blankEntities() = Entities.getInstance()

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }

    private fun integrate() {
        logger.debug { "Integrating" }
        val tick = getCurrentTick()
        logger.debug { "Tick = $tick" }

        fetchInput()
        // tick is advanced inside;
        clientSimulation.tick()
        logger.info { "About to unpack delta and apply to ${tick + 1}" }
        stateSnapper.unpackDeltaRecipe(state.entities(atTick = tick + 1), incomingDataBuffer.deltas[tick]!!)

        logger.info { "Authoritative position: ${state.entities(getCurrentTick()).all().find { it.id == AuthoritativeEntityId(3) }
            ?.getComponent(Class.forName("com.mgtriffid.games.panna.shared.game.components.PositionComponent").kotlin as KClass<out Component<*>>)}" }
        predict()
        logger.info { "Predicted position: ${predictionSimulation.getPredictedEntities().find { it.id == PredictedEntityId(PlayerId(1), 1) }
            ?.getComponent(Class.forName("com.mgtriffid.games.panna.shared.game.components.PositionComponent").kotlin as KClass<out Component<*>>)}" }
        logger.info { "Predicted position: ${predictionSimulation.getPredictedEntities().find { it.id == AuthoritativeEntityId(3) }
            ?.getComponent(Class.forName("com.mgtriffid.games.panna.shared.game.components.PositionComponent").kotlin as KClass<out Component<*>>)}" }
        sendDataToServer()
    }

    // called after advancing tick
    private fun sendDataToServer() {
        // since this method is called after advancing tick, we need to send inputs for the previous tick
        val inputs = clientInputs.get(tickProvider.tick - 1)
        val createdEntities = predictedCreatedEntitiesRegistry.find(tickProvider.tick)
        send(inputs, createdEntities)
    }

    private fun predict() {
        val playerId = (metaEntity()?.ownedBy as? Entity.OwnedBy.Player)?.playerId
        if (playerId == null) {
            logger.warn { "No player id, we should not even be simulating. Returning." }
            return
        }
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
        val lastMyInputProcessedByServerSimulation =
            incomingDataBuffer.playersSawTicks[currentTick - 1]!![playerId]?.let {
                logger.debug { "Got $it as processed input from Server" }
                it
            } ?: 0L.also {
                logger.debug { "Did not find our input in server's input, assuming none of our input was processed yet" }
            }// TODO gracefully handle missing
        val unprocessedTicks = clientInputs.all().keys.filter { it > lastMyInputProcessedByServerSimulation }
            .also { logger.info { it.joinToString() } } // TODO explicit sorting
        logger.info {
            "inputs: " + unprocessedTicks.joinToString { tick ->
                "$tick ${clientInputs.all()[tick]?.inputs?.get(AuthoritativeEntityId(3))?.toString()}"
            }
        }
        logger.info { "Setting initial predictions state with tick ${getCurrentTick()}" }
        predictionSimulation.startPredictionFrom(
            state.entities(currentTick),
            unprocessedTicks.min()
        )
        predictionSimulation.run(unprocessedTicks, playerId)
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
        val inputs = gatherLocalInput(metaEntity)
        clientInputs.store(inputs)
    }

    private fun gatherLocalInput(metaEntity: Entity): ClientInput {
        val player = (metaEntity.ownedBy as Entity.OwnedBy.Player)
        val localEntities = getEntitiesOwnedByPlayer(player)
        logger.debug { "Found ${localEntities.size} entities owned by player $player" }
        val localEntitiesWithInputComponents = localEntities.filter {
            it.hasInputComponents()
        }
        logger.debug { "Found ${localEntitiesWithInputComponents.size} entities with input components" }
        val inputs = localEntitiesWithInputComponents.associate { e ->
            logger.debug { "Retrieving input for entity '${e.id}'" }
            e.id to getInputs(e)
        }
        return ClientInputImpl(inputs)
    }

    private fun send(inputs: ClientInput, createdEntities: List<Pair<CottaTrace, EntityId>>) {
        val inputRecipe = inputSnapper.snapInput(inputs.inputs)
        val createdEntitiesRecipe = createdEntities.map { (trace, id) ->
            Pair(stateSnapper.snapTrace(trace), id)
        }

        val inputDto = ClientToServerInputDto()
        inputDto.tick = getCurrentTick() - 1
        inputDto.payload = inputSerialization.serializeInputRecipe(inputRecipe)
        network.sendInput(inputDto)
        val createdEntitiesDto = ClientToServerCreatedPredictedEntitiesDto()
        createdEntitiesDto.tick = getCurrentTick()
        createdEntitiesDto.payload = snapsSerialization.serializeEntityCreationTraces(createdEntitiesRecipe)
        network.sendCreatedEntities(createdEntitiesDto)
    }

    private fun getInputs(entity: Entity) = entity.inputComponents().map { clazz ->
        logger.debug { "Retrieving input of class '${clazz.simpleName}' for entity '${entity.id}'" }
        localInput.input(entity, clazz)
    }

    private fun getEntitiesOwnedByPlayer(player: Entity.OwnedBy.Player) =
        state.entities(atTick = getCurrentTick()).all().filter {
            it.ownedBy == player
        } + predictionSimulation.getLocalPredictedEntities()

    private fun metaEntity(): Entity? {
        logger.debug { "Looking for meta entity, metaEntityId = $metaEntityId" }
        val entity = state.entities(atTick = getCurrentTick()).all().find {
            it.id == metaEntityId
        }
        if (entity != null) {
            logger.debug { "Meta entity found" }
        } else {
            logger.debug { "Meta entity not found" }
        }
        return entity
    }

    private fun fetchData() {
        val data = network.drainIncomingData()
        data.forEach {
            when (it.kindOfData) {
                KindOfData.DELTA -> incomingDataBuffer.storeDelta(
                    it.tick,
                    snapsSerialization.deserializeDeltaRecipe(it.payload)
                )

                KindOfData.STATE -> incomingDataBuffer.storeState(
                    it.tick,
                    snapsSerialization.deserializeStateRecipe(it.payload)
                )

                KindOfData.CLIENT_META_ENTITY_ID -> {
                    val (entityId, playerId) = snapsSerialization.deserializeMetaEntityId(it.payload)
                    metaEntityId = entityId
                    playerIdHolder.playerId = playerId
                }

                KindOfData.INPUT -> incomingDataBuffer.storeInput(
                    it.tick,
                    inputSerialization.deserializeInputRecipe(it.payload)
                )

                KindOfData.CREATED_ENTITIES -> incomingDataBuffer.storeCreatedEntities(
                    it.tick,
                    snapsSerialization.deserializeEntityCreationTraces(it.payload)
                )

                KindOfData.PLAYERS_SAW_TICKS -> incomingDataBuffer.storePlayersSawTicks(
                    it.tick,
                    snapsSerialization.deserializePlayersSawTicks(it.payload)
                )

                KindOfData.CREATED_ENTITIES_V2 -> incomingDataBuffer.storeCreatedEntitiesV2(
                    it.tick,
                    snapsSerialization.deserializeEntityCreationTracesV2(it.payload)
                )

                null -> throw IllegalStateException("kindOfData is null in an incoming ServerToClientDto")
            }
        }
    }

    // should not use these anywhere but awaiting game state
    private fun stateAvailable(): Boolean {
        val stateArrived = incomingDataBuffer.states.isNotEmpty()
        if (!stateArrived) return false
        val stateTick = incomingDataBuffer.states.lastKey()
        val deltasForLagCompArrived =
            incomingDataBuffer.deltas.keys.containsAll(((stateTick + 1)..(stateTick + lagCompLimit + bufferLength)).toList())
        return deltasForLagCompArrived
    }

    private fun deltaAvailableForTick(tick: Long): Boolean {
        return incomingDataBuffer.deltas.containsKey(tick).also { logger.debug { "Delta present for tick $tick: $it" } }
            && incomingDataBuffer.inputs.containsKey(tick).also { logger.debug { "Input present for tick $tick: $it" } }
            && incomingDataBuffer.playersSawTicks.containsKey(tick).also { logger.debug { "sawTicks present for tick $tick: $it" } }
            && incomingDataBuffer.createdEntitiesV2.containsKey(tick).also { logger.debug { "createEntities present for tick $tick: $it" } }
    }

    private fun connect() {
        network.initialize()
        network.sendEnterGameIntent()
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
