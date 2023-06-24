package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.protocol.ClientToServerInputDto
import com.mgtriffid.games.cotta.network.protocol.KindOfData
import com.mgtriffid.games.cotta.utils.now
import mu.KotlinLogging
import java.lang.IllegalStateException

const val STATE_WAITING_THRESHOLD = 5000L

private val logger = KotlinLogging.logger {}

class CottaClientImpl<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe>(
    val game: CottaGame,
    val engine: CottaEngine<SR, DR, IR>, // weird type parameterization
    val network: CottaClientNetwork,
    val input: CottaClientInput,
    val lagCompLimit: Int,
    val bufferLength: Int
) : CottaClient {
    var connected = false
    private val incomingDataBuffer = IncomingDataBuffer<SR, DR, IR>()
    private var state: ClientState = ClientState.Initial
    private val componentsRegistry = engine.getComponentsRegistry()
    private val stateSnapper = engine.getStateSnapper()
    private val snapsSerialization = engine.getSnapsSerialization()
    private val inputSnapper = engine.getInputSnapper()
    private val inputSerialization = engine.getInputSerialization()
    private val tickProvider = AtomicLongTickProvider()
    val cottaState = CottaState.getInstance(tickProvider)
    private var metaEntityId: EntityId? = null

    override fun initialize() {
        registerComponents()
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
                        integrate()
                        state = ClientState.Running(it.currentTick + 1)
                    } else {
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
        cottaState.setBlank(fullStateTick)
        tickProvider.tick = fullStateTick
        stateSnapper.unpackStateRecipe(cottaState.entities(atTick = fullStateTick), stateRecipe)
        ((fullStateTick + 1)..(fullStateTick + lagCompLimit)).forEach { tick ->
            cottaState.advance()
            stateSnapper.unpackDeltaRecipe(cottaState.entities(atTick = tick), incomingDataBuffer.deltas[tick]!!)
        }
    }

    private fun getCurrentTick(): Long {
        return tickProvider.tick
    }

    private fun integrate() {
        logger.debug { "Integrating" }
        cottaState.advance()
        val tick = getCurrentTick()
        logger.debug { "Tick = $tick" }

        processInput()

        stateSnapper.unpackDeltaRecipe(cottaState.entities(atTick = tick), incomingDataBuffer.deltas[tick]!!)

    }

    private fun processInput() {
        val player = ((cottaState.entities(atTick = getCurrentTick()).all().find {
            it.id == metaEntityId
        } ?: return).ownedBy as Entity.OwnedBy.Player)
        val inputs = cottaState.entities(atTick = getCurrentTick()).all().filter {
            it.ownedBy == player
        }.filter {
            it.hasInputComponents()
        }.associate { e ->
            e.id to e.inputComponents().map { clazz -> input.input(e, clazz) }
        }
        val inputRecipe = inputSnapper.snapInput(inputs)
        val inputDto = ClientToServerInputDto()
        inputDto.tick = getCurrentTick()
        inputDto.payload = inputSerialization.serializeInputRecipe(inputRecipe)
        network.sendInput(inputDto)
    }

    private fun fetchData() {
        val data = network.drainIncomingData()
        data.forEach {
            when (it.kindOfData) {
                KindOfData.DELTA -> incomingDataBuffer.storeDelta(it.tick, snapsSerialization.deserializeDeltaRecipe(it.payload))
                KindOfData.STATE -> incomingDataBuffer.storeState(it.tick, snapsSerialization.deserializeStateRecipe(it.payload))
                KindOfData.CLIENT_META_ENTITY_ID -> metaEntityId = snapsSerialization.deserializeEntityId(it.payload)
                KindOfData.INPUT -> incomingDataBuffer.storeInput(it.tick, inputSerialization.deserializeInputRecipe(it.payload))
                null -> throw IllegalStateException("kindOfData is null in an incoming ServerToClientDto")
            }
        }
    }

    // should not use these anywhere but awaiting game state
    private fun stateAvailable(): Boolean {
        val stateArrived = incomingDataBuffer.states.isNotEmpty()
        if (!stateArrived) return false
        val stateTick = incomingDataBuffer.states.lastKey()
        return incomingDataBuffer.deltas.keys.containsAll(((stateTick + 1)..(stateTick + lagCompLimit + bufferLength)).toList())
    }

    private fun deltaAvailableForTick(tick: Long): Boolean {
        return incomingDataBuffer.deltas.containsKey(tick)
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
    }

    sealed class ClientState {
        object Initial : ClientState()
        class AwaitingGameState(val since: Long) : ClientState()
        object Disconnected : ClientState()
        class Running(val currentTick: Long) : ClientState() // not sure again
    }
}
