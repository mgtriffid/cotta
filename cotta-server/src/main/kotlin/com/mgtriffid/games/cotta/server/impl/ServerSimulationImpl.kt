package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity.OwnedBy
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.server.IncomingInput
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.server.ServerSimulation
import com.mgtriffid.games.cotta.server.impl.invokers.HistoricalLagCompensatingEffectBus
import com.mgtriffid.games.cotta.server.impl.invokers.InvokersFactory
import com.mgtriffid.games.cotta.server.impl.invokers.InvokersFactoryImpl
import com.mgtriffid.games.cotta.server.impl.invokers.LagCompensatingEffectBusImpl
import com.mgtriffid.games.cotta.server.impl.invokers.SystemInvoker
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class ServerSimulationImpl(
    private val tickProvider: TickProvider,
    private val historyLength: Int
) : ServerSimulation {
    private val systemInvokers = ArrayList<SystemInvoker>()

    private val entityOwners = HashMap<EntityId, PlayerId>()
    private val playersSawTicks = HashMap<PlayerId, Long>()

    private val enterGameIntents = ArrayList<Pair<EnterGameIntent, PlayerId>>()
    private val playerIdGenerator = PlayerIdGenerator()
    private val metaEntities = HashMap<PlayerId, EntityId>()
    // TODO stuff this with inputs from clients
    private var inputForUpcomingTick: IncomingInput = object: IncomingInput {
        override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
            return emptyMap()
        }
    }
    private val effectBus = EffectBus.getInstance()

    private lateinit var state: CottaState
    private lateinit var invokersFactory: InvokersFactory
    private val effectsHistory = EffectsHistory(historyLength = historyLength)

    override fun effectBus(): EffectBus {
        return effectBus
    }

    // TODO add validation to call this exactly once
    override fun setState(state: CottaState) {
        this.state = state
        val sawTickHolder = InvokersFactoryImpl.SawTickHolder(null)
        // TODO decouple
        this.invokersFactory = InvokersFactory.getInstance(
            HistoricalLagCompensatingEffectBus(
                history = effectsHistory,
                impl = LagCompensatingEffectBusImpl(effectBus, sawTickHolder),
                tickProvider = tickProvider
            ),
            state,
            entityOwners,
            playersSawTicks,
            tickProvider,
            sawTickHolder
        )
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        logger.debug { "Registering system '${systemClass.simpleName}'" }
        systemInvokers.add(createInvoker(systemClass))
    }

    override fun setInputForUpcomingTick(input: IncomingInput) {
        inputForUpcomingTick = input
    }

    override fun tick() {
        effectBus.clear()
        state.advance()
        processEnterGameIntents()
        putInputIntoEntities()
        for (invoker in systemInvokers) {
            invoker()
        }
    }

    private fun putInputIntoEntities() {
        state.entities().all().filter {
            it.hasInputComponents()
        }.forEach { e ->
            e.inputComponents().forEach { c ->
                e.setInputComponent(c, inputForUpcomingTick.inputForEntityAndComponent(e.id, c))
            }
        }
    }

    override fun setEntityOwner(entityId: EntityId, playerId: PlayerId) {
        entityOwners[entityId] = playerId
    }

    override fun setPlayerSawTick(playerId: PlayerId, tick: Long) {
        playersSawTicks[playerId] = tick
    }

    override fun enterGame(intent: EnterGameIntent): PlayerId {
        val playerId = playerIdGenerator.nextId()
        enterGameIntents.add(Pair(intent, playerId))
        return playerId
    }

    override fun getDataToBeSentToClients(): DataForClients {
        return DataForClientsImpl(
            effectsHistory = effectsHistory,
            inputs = inputForUpcomingTick.inputsForEntities(),
            state = state,
            metaEntities = metaEntities
        )
    }

    private fun processEnterGameIntents() {
        enterGameIntents.forEach {
            val metaEntity = state.entities().createEntity(ownedBy = OwnedBy.Player(it.second))
            val playerId = it.second
            metaEntities[playerId] = metaEntity.id
            entityOwners[metaEntity.id] = playerId
            it.first.params // TODO use parameters to add certain components, figure it out
        }
        enterGameIntents.clear()
    }

    private fun <T : CottaSystem> createInvoker(systemClass: KClass<T>): SystemInvoker {
        return invokersFactory.createInvoker(systemClass)
    }

    private class PlayerIdGenerator {
        val counter = AtomicInteger(0)

        fun nextId(): PlayerId = PlayerId(counter.incrementAndGet())
    }
}
