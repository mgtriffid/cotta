package com.mgtriffid.games.cotta.server.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity.OwnedBy
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreatedEntities
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.MetaEntities
import com.mgtriffid.games.cotta.server.ServerSimulation
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class ServerSimulationImpl @Inject constructor(
    private val state: CottaState,
    private val simulationInputHolder: SimulationInputHolder,
    private val metaEntities: MetaEntities,
    private val invokersFactory: InvokersFactory,
    private val effectBus: EffectBus,
    private val createdEntities: CreatedEntities,
    private val effectsHistory: EffectsHistory
) : ServerSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>()

    private val enterGameIntents = ArrayList<Pair<EnterGameIntent, PlayerId>>()
    private val playerIdGenerator = PlayerIdGenerator()
    private lateinit var metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>>

    override fun effectBus(): EffectBus {
        return effectBus
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        logger.info { "Registering system '${systemClass.simpleName}'" }
        systemInvokers.add(invokersFactory.createInvoker(systemClass))
    }

    override fun setMetaEntitiesInputComponents(components: Set<KClass<out InputComponent<*>>>) {
        this.metaEntitiesInputComponents = components;
    }

    override fun tick() {
        effectBus.clear()
        state.advance()
        putInputIntoEntities()
        for ((invoker, system) in systemInvokers) {
            (invoker as SystemInvoker<CottaSystem>).invoke(system) // TODO cast issue
        }
        processEnterGameIntents()
    }

    private fun putInputIntoEntities() {
        state.entities().all().filter {
            it.hasInputComponents()
        }.forEach { e ->
            logger.trace { "Entity ${e.id} has some input components:" }
            e.inputComponents().forEach { c ->
                val component = simulationInputHolder.get().inputForEntityAndComponent(e.id, c)
                logger.trace { "  $component" }
                e.setInputComponent(c, component)
            }
        }
    }

    override fun enterGame(intent: EnterGameIntent): PlayerId {
        val playerId = playerIdGenerator.nextId()
        enterGameIntents.add(Pair(intent, playerId))
        return playerId
    }

    override fun getDataToBeSentToClients(): DataForClients {
        return DataForClientsImpl(
            effectsHistory = effectsHistory,
            inputs = simulationInputHolder.get().inputsForEntities(),
            state = state,
            createdEntities = createdEntities,
            metaEntities = metaEntities
        )
    }

    private fun processEnterGameIntents() {
        enterGameIntents.forEach {
            val metaEntity = state.entities().createEntity(ownedBy = OwnedBy.Player(it.second))
            metaEntitiesInputComponents.forEach { componentClass ->
                metaEntity.addInputComponent(componentClass)
            }
            val playerId = it.second
            metaEntities[playerId] = metaEntity.id
            it.first.params // TODO use parameters to add certain components, figure it out
        }
        enterGameIntents.clear()
    }

    private class PlayerIdGenerator {
        val counter = AtomicInteger(0)

        fun nextId(): PlayerId = PlayerId(counter.incrementAndGet())
    }
}
