package com.mgtriffid.games.cotta.client.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.client.*
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
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.protocol.KindOfData
import com.mgtriffid.games.cotta.utils.now
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

const val STATE_WAITING_THRESHOLD = 5000L

private val logger = KotlinLogging.logger {}

class CottaClientImpl<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> @Inject constructor(
    val game: CottaGame,
    val engine: CottaEngine<SR, DR, IR>, // weird type parameterization
    val network: CottaClientNetwork,
    val localInput: CottaClientInput,
    val clientInputs: ClientInputs,
    val clientSimulation: ClientSimulation,
    private val predictionSimulation: PredictionSimulation,
    val input: ClientSimulationInputProvider,
    val tickProvider: TickProvider,
    private val incomingDataBuffer: IncomingDataBuffer<SR, DR, IR>,
    @Named("simulation") val cottaState: CottaState // Todo not expose as public
) : CottaClient {
    val lagCompLimit: Int = 8 // TODO move to config and bind properly
    val bufferLength: Int = 3
    private var state: ClientState = ClientState.Initial
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
        logger.info { "Running ${CottaClientImpl::class.simpleName}" }
        state.let {
            when (it) {
                ClientState.Initial -> {
                    connect()
                    state = ClientState.AwaitingGameState(since = System.currentTimeMillis())
                }

                is ClientState.AwaitingGameState -> {
                    fetchData()
                    // TODO ensure that if metaEntity did not come we still can operate
                    if (stateAvailable()) {
                        setStateFromAuthoritative()
                        state = ClientState.Running(getCurrentTick())
                    } else {
                        if (now() - it.since > STATE_WAITING_THRESHOLD) {
                            state = ClientState.Disconnected
                        }
                    }
                }

                ClientState.Disconnected -> {
                    // TODO
                }

                is ClientState.Running -> {
                    fetchData()
                    if (deltaAvailableForTick(getCurrentTick())) {
                        logger.trace { "Delta available, we will integrate" }
                        integrate()
                        state = ClientState.Running(it.currentTick + 1)
                    } else {
                        logger.trace { "Delta not available" }
                        // for now do nothing, later we'll guess and keep track of how
                        // long ago did we have a state that is trusted
                    }
                }
            }
        }
    }

    private fun setStateFromAuthoritative() {
        logger.debug { "Setting state from authoritative" }
        val fullStateTick = incomingDataBuffer.states.lastKey()
        val stateRecipe = incomingDataBuffer.states[fullStateTick]!!
        cottaState.set(fullStateTick, blankEntities())
        tickProvider.tick = fullStateTick
        stateSnapper.unpackStateRecipe(cottaState.entities(atTick = fullStateTick), stateRecipe)
        ((fullStateTick + 1)..(fullStateTick + lagCompLimit)).forEach { tick ->
            cottaState.advance(tick - 1)
            tickProvider.tick++
            stateSnapper.unpackDeltaRecipe(cottaState.entities(atTick = tick), incomingDataBuffer.deltas[tick]!!)
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
        stateSnapper.unpackDeltaRecipe(cottaState.entities(atTick = tick + 1), incomingDataBuffer.deltas[tick]!!)

//        predict()

        sendDataToServer()
    }

    private fun sendDataToServer() {
        val inputs = clientInputs.get(tickProvider.tick - 1)
        send(inputs)
    }

    private fun predict() {
        val playerId = (metaEntity()?.ownedBy as? Entity.OwnedBy.Player)?.playerId
        if (playerId == null) {
            logger.warn { "No player id, we should not even be simulating. Returning." }
            return
        }
        logger.debug { "Predicting" }
        val currentTick = getCurrentTick()
        val lastMyInputProcessedByServerSimulation = incomingDataBuffer.playersSawTicks[currentTick - 1]!![playerId]?.let {
            logger.debug { "Got $it as processed input from Server" }
            it
        } ?: 0L.also {
            logger.debug { "Did not find our input in server's input, assuming none of our input was processed yet" }
        }// TODO gracefully handle missing
        val unprocessedTicks = clientInputs.all().keys.filter { it > lastMyInputProcessedByServerSimulation }
            .also { logger.debug { it.joinToString() } } // TODO explicit sorting
        predictionSimulation.startPredictionFrom(cottaState.entities(atTick = currentTick), currentTick)
        predictionSimulation.run(unprocessedTicks, playerId)
    }

    private fun fetchInput() {
        processLocalInput()

        input.prepare()
    }

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

    private fun send(inputs: ClientInput) {
        val inputRecipe = inputSnapper.snapInput(inputs.inputs)
        val inputDto = ClientToServerInputDto()
        inputDto.tick = getCurrentTick()
        inputDto.payload = inputSerialization.serializeInputRecipe(inputRecipe)
        network.sendInput(inputDto)
    }

    private fun getInputs(entity: Entity) = entity.inputComponents().map { clazz ->
        logger.debug { "Retrieving input of class '${clazz.simpleName}' for entity '${entity.id}'" }
        localInput.input(entity, clazz)
    }

    private fun getEntitiesOwnedByPlayer(player: Entity.OwnedBy.Player) =
        cottaState.entities(atTick = getCurrentTick()).all().filter {
            it.ownedBy == player
        }

    private fun metaEntity(): Entity? {
        logger.debug { "Looking for meta entity, metaEntityId = $metaEntityId" }
        val entity = cottaState.entities(atTick = getCurrentTick()).all().find {
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
                KindOfData.DELTA -> incomingDataBuffer.storeDelta(it.tick, snapsSerialization.deserializeDeltaRecipe(it.payload))
                KindOfData.STATE -> incomingDataBuffer.storeState(it.tick, snapsSerialization.deserializeStateRecipe(it.payload))
                KindOfData.CLIENT_META_ENTITY_ID -> metaEntityId = snapsSerialization.deserializeEntityId(it.payload)
                KindOfData.INPUT -> incomingDataBuffer.storeInput(it.tick, inputSerialization.deserializeInputRecipe(it.payload))
                KindOfData.CREATED_ENTITIES -> incomingDataBuffer.storeCreatedEntities(it.tick, snapsSerialization.deserializeEntityCreationTraces(it.payload))
                KindOfData.PLAYERS_SAW_TICKS -> incomingDataBuffer.storePlayersSawTicks(it.tick, snapsSerialization.deserializePlayersSawTicks(it.payload))
                null -> throw IllegalStateException("kindOfData is null in an incoming ServerToClientDto")
            }
        }
    }

    // should not use these anywhere but awaiting game state
    private fun stateAvailable(): Boolean {
        val stateArrived = incomingDataBuffer.states.isNotEmpty()
        if (!stateArrived) return false
        val stateTick = incomingDataBuffer.states.lastKey()
        val deltasForLagCompArrived = incomingDataBuffer.deltas.keys.containsAll(((stateTick + 1)..(stateTick + lagCompLimit + bufferLength)).toList())
        return deltasForLagCompArrived
    }

    private fun deltaAvailableForTick(tick: Long): Boolean {
        return incomingDataBuffer.deltas.containsKey(tick)
            && incomingDataBuffer.inputs.containsKey(tick)
            && incomingDataBuffer.playersSawTicks.containsKey(tick)
            && incomingDataBuffer.createdEntities.containsKey(tick)
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
