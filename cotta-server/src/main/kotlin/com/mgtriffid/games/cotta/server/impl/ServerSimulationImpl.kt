package com.mgtriffid.games.cotta.server.impl

import com.google.inject.Inject
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity.OwnedBy
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.server.MetaEntities
import com.mgtriffid.games.cotta.server.ServerSimulation
import jakarta.inject.Named
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class ServerSimulationImpl @Inject constructor(
    @Named("simulation") private val state: CottaState,
    private val metaEntities: MetaEntities,
    private val invokersFactory: InvokersFactory,
    private val effectBus: EffectBus,
    private val playersSawTicks: PlayersSawTicks,
    private val tickProvider: TickProvider,
    private val inputProcessing: InputProcessing
) : ServerSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>()

    private val enterGameIntents = ArrayList<Pair<EnterGameIntent, PlayerId>>()
    private val playerIdGenerator = PlayerIdGenerator()
    private lateinit var metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>>

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        logger.info { "Registering system '${systemClass.simpleName}'" }
        systemInvokers.add(invokersFactory.createInvoker(systemClass))
    }

    override fun setMetaEntitiesInputComponents(components: Set<KClass<out InputComponent<*>>>) {
        this.metaEntitiesInputComponents = components;
    }

    override fun tick(input: SimulationInput) {
        effectBus.clear()
        state.advance(tickProvider.tick)
        logger.debug { "Advancing tick from ${tickProvider.tick} to ${tickProvider.tick + 1}" }
        tickProvider.tick++
        putInputIntoEntities(input)
        processInput(input)
        fillPlayersSawTicks(input)
        simulate()
        processEnterGameIntents()
    }

    private fun processInput(input: SimulationInput) {
        inputProcessing.process(
            input,
            state.entities(tickProvider.tick),
            effectBus
        )
    }

    private fun simulate() {
        for ((invoker, system) in systemInvokers) {
            (invoker as SystemInvoker<CottaSystem>).invoke(system) // TODO cast issue
        }
    }

    private fun fillPlayersSawTicks(input: SimulationInput) {
        playersSawTicks.set(input.playersSawTicks())
    }

    private fun putInputIntoEntities(input: SimulationInput) {
        state.entities(tickProvider.tick).all().filter {
            it.hasInputComponents()
        }.forEach { e ->
            logger.trace { "Entity ${e.id} has some input components:" }
            e.inputComponents().forEach { c ->
                val component = input.inputForEntityAndComponent(e.id, c)
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

    private fun processEnterGameIntents() {
        logger.debug { "Processing enterGameIntents" }
        enterGameIntents.forEach {
            logger.debug { "Processing intent to ETG for ${it.second}" }
            val metaEntity = state.entities(tickProvider.tick).create(ownedBy = OwnedBy.Player(it.second))
            metaEntitiesInputComponents.forEach { componentClass ->
                metaEntity.addInputComponent(componentClass)
            }
            val playerId = it.second
            metaEntities[playerId] = metaEntity.id
            it.first.params // TODO use parameters to add certain components, figure it out
        }
        metaEntities.recordNew(enterGameIntents.map { metaEntities[it.second] to it.second }, tickProvider.tick)
        enterGameIntents.clear()
    }

    private class PlayerIdGenerator {
        val counter = AtomicInteger(0)

        fun nextId(): PlayerId = PlayerId(counter.incrementAndGet())
    }
}
