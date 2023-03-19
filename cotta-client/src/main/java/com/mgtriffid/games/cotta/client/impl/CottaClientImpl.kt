package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import com.mgtriffid.games.cotta.network.protocol.KindOfData
import com.mgtriffid.games.cotta.utils.now
import mu.KotlinLogging
import java.lang.IllegalStateException

const val STATE_WAITING_THRESHOLD = 5000L

private val logger = KotlinLogging.logger {}

class CottaClientImpl<SR: StateRecipe, DR: DeltaRecipe>(
    val game: CottaGame,
    val engine: CottaEngine<SR, DR>, // weird type parameterization
    val network: CottaClientNetwork,
    val lagCompLimit: Int,
    val bufferLength: Int
) : CottaClient {
    var connected = false
    private val incomingDataBuffer = IncomingDataBuffer<SR, DR>()
    private var state: ClientState = ClientState.Initial
    private val componentsRegistry = engine.getComponentsRegistry()
    private val stateSnapper = engine.getStateSnapper()
    private val snapsSerialization = engine.getSnapsSerialization()
    private val tickProvider = AtomicLongTickProvider()
    private val cottaState = CottaState.getInstance(tickProvider)

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
//        TODO("Not yet implemented")
        return tickProvider.tick
    }

    private fun integrate() {
        logger.debug { "Integrating" }
        // take input
        // integrate using rules

        cottaState.advance()
        val tick = getCurrentTick()
        logger.debug { "Tick=$tick" }
        stateSnapper.unpackDeltaRecipe(cottaState.entities(atTick = tick), incomingDataBuffer.deltas[tick]!!)
    }

    private fun fetchData() {
        val data = network.drainIncomingData()
        data.forEach {
            when (it.kindOfData) {
                KindOfData.DELTA -> incomingDataBuffer.storeDelta(it.tick, snapsSerialization.deserializeDeltaRecipe(it.payload))
                KindOfData.STATE -> incomingDataBuffer.storeState(it.tick, snapsSerialization.deserializeStateRecipe(it.payload))
                null -> throw IllegalStateException("kindOfData is null in an incoming ServerToClientDto")
            }
        }
        // take data from queues and put it into buffers, deserialize, etc.
        // data can be of two kinds (so far): state packets, delta packets.
        // it also can be absent
        // we stuff it in and then make a decision what to do.
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
