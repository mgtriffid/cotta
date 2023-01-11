package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.server.EnterGameIntent
import com.mgtriffid.games.cotta.server.PlayerId
import com.mgtriffid.games.cotta.server.ServerSimulation
import com.mgtriffid.games.cotta.server.impl.invokers.InvokersFactory
import com.mgtriffid.games.cotta.server.impl.invokers.SystemInvoker
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class ServerSimulationImpl: ServerSimulation {
    private val systemInvokers = ArrayList<SystemInvoker>()

    private val entityOwners = HashMap<Int, PlayerId>()
    private val playersSawTicks = HashMap<PlayerId, Long>()

    private val enterGameIntents = ArrayList<Pair<EnterGameIntent, PlayerId>>()
    private val playerIdGenerator = PlayerIdGenerator()
    private val metaEntities = HashMap<PlayerId, Int>()

    private val effectBus = EffectBus.getInstance()

    private val clientsGhosts = ClientsGhosts()

    private lateinit var state: CottaState
    private lateinit var invokersFactory: InvokersFactory

    override fun effectBus(): EffectBus {
        return effectBus
    }

    override fun setState(state: CottaState) {
        this.state = state
        // TODO decouple
        this.invokersFactory = InvokersFactory.getInstance(
            effectBus,
            state,
            entityOwners,
            playersSawTicks
        )
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        logger.debug { "Registering system '${systemClass.simpleName}'" }
        systemInvokers.add(createInvoker(systemClass))
    }

    override fun tick() {
        state.advance()
        processEnterGameIntents()
        putInputIntoEntities()
        for (invoker in systemInvokers) {
            invoker()
        }
        effectBus.clear()
    }

    private fun putInputIntoEntities() {
        // here be taking input and stuffing it into InputComponents of corresponding entities
    }

    override fun setEntityOwner(entityId: Int, playerId: PlayerId) {
        entityOwners[entityId] = playerId
    }

    override fun setPlayerSawTick(playerId: PlayerId, tick: Long) {
        playersSawTicks[playerId] = tick
    }

    override fun enterGame(intent: EnterGameIntent): PlayerId {
        val playerId = playerIdGenerator.nextId()
        enterGameIntents.add(Pair(intent, playerId))
        clientsGhosts.addGhost(playerId)
        return playerId
    }

    private fun processEnterGameIntents() {
        enterGameIntents.forEach {
            val metaEntity = state.entities().createEntity()
            val playerId = it.second
            metaEntities[playerId] = metaEntity.id
            entityOwners[metaEntity.id] = playerId
            it.first.params // TODO use parameters to add certain components, figure it out
            sendMetaEntity(playerId, metaEntity.id)
            sendStates(playerId)
        }
        enterGameIntents.clear()
    }

    private fun sendMetaEntity(playerId: PlayerId, metaEntityId: Int) {

    }

    private fun sendStates(playerId: PlayerId) {

    }

    private fun <T : CottaSystem> createInvoker(systemClass: KClass<T>): SystemInvoker {
        return invokersFactory.createInvoker(systemClass)
    }

    private class PlayerIdGenerator {
        val counter = AtomicInteger(0)

        fun nextId(): PlayerId = PlayerId(counter.incrementAndGet())
    }
}
