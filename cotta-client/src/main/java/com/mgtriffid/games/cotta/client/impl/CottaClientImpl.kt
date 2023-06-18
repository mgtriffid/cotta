package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.EntityId
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
    val input: CottaClientInput,
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
        cottaState.advance()
        val tick = getCurrentTick()
        logger.debug { "Tick = $tick" }
        // input first, only then delta
        // we need to get our local input
        // Will it be like "find all Entities that are of this player, then try to call input provider for it"?
        // Or will it be like "find all Entities that have InputComponent, then call input provider for it"?
        // BOTH! They should have both InputComponent and ownedBy == this player.
        // and also somewhere around here should be the effects
        // and we also need other clients' inputs
        // and we need to SMEAR this with logs all over the place!
        stateSnapper.unpackDeltaRecipe(cottaState.entities(atTick = tick), incomingDataBuffer.deltas[tick]!!)
        // now on top of what just happened we also predict shit using local inputs
        // input is a map of EntityId to (List?) <InputComponent>, we push certain input to Server. For example,
        // MetaEntityId to "LetDudeEnterTheGame" or our battling dude to (Direction, Shoots, Jumps) = (RIGHT, false)
        // How do we find those IDs? Need to figure out the signature for that function that takes state and input, then
        // creates that map of entity to input components. So for example for an FPS game we will take those Entities that
        // are ours, then we'll find one that has component like FightingDudeComponent, and we'll stuff input into it.
        // Sensible so far. So the signature is: input(entities: List<Entity>): Map<>. Another option is input(entity: Entity) and it
        // is called multiple times, according to the number of local entities Player controls.
        // takeInput()
        // processPrediction() // here we operate on that predicted state that co-exists with real state
        // sendInput()
    }

    private fun fetchData() {
        val data = network.drainIncomingData()
        data.forEach {
            when (it.kindOfData) {
                KindOfData.DELTA -> incomingDataBuffer.storeDelta(it.tick, snapsSerialization.deserializeDeltaRecipe(it.payload))
                KindOfData.STATE -> incomingDataBuffer.storeState(it.tick, snapsSerialization.deserializeStateRecipe(it.payload))
                KindOfData.CLIENT_META_ENTITY_ID -> metaEntityId = snapsSerialization.deserializeEntityId(it.payload)
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
