package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeToPredictedEntityIdMappings
import com.mgtriffid.games.cotta.client.ClientInputs
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesImpl
import com.mgtriffid.games.cotta.core.input.ClientInput
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

// TODO test this, without unit tests it's super fragile
class PredictionSimulationImpl @Inject constructor(
    @Named("prediction") private val invokersFactory: InvokersFactory,
    @Named("prediction") private val state: CottaState,
    private val clientInputs: ClientInputs,
    @Named("prediction") private val effectBus: EffectBus,
    @Named("prediction") private val tickProvider: TickProvider,
    private val localPlayer: LocalPlayer,
    private val idMappings: AuthoritativeToPredictedEntityIdMappings
) : PredictionSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>()

    override fun predict(initialEntities: Entities, ticks: List<Long>) {
        startPredictionFrom(initialEntities, ticks.first())
        run(ticks)
    }

    private fun run(ticks: List<Long>) {
        logger.debug { "Running prediction simulation for ticks $ticks" }
        for (tick in ticks) {
            logger.info { "Running prediction simulation for tick $tick" }
            effectBus.clear()
            state.advance(tickProvider.tick)
            tickProvider.tick++
            putInputIntoEntities(tick, localPlayer.playerId)
            simulate()
        }
    }

    private fun putInputIntoEntities(tick: Long, playerId: PlayerId) {
        logger.debug { "Putting input into entities for tick $tick" }
        val input = clientInputs.get(tick)
        state.entities(tickProvider.tick).all().filter {
            it.ownedBy == Entity.OwnedBy.Player(playerId) && it.hasInputComponents()
        }.forEach { e ->
            logger.trace { "Entity ${e.id} has some input components:" }
            e.inputComponents().forEach { c ->
                val component = input.inputForEntityAndComponent(e.id, c)
                logger.trace { "  $component" }
                e.setInputComponent(c, component)
            }
        }
    }

    private fun ClientInput.inputForEntityAndComponent(entityId: EntityId, component: KClass<*>): InputComponent<*> {
        logger.trace { "Getting input for entity $entityId and component ${component.qualifiedName}" }
        val entityInputs = getEntityInputs(entityId)
        val input = entityInputs?.find { component.isInstance(it) }
        if (input != null) {
            logger.trace { "Input found" }
            return input
        } else {
            logger.trace { "Input not found, falling back" }
            return fallback(component)
        }
    }

    private fun ClientInput.getEntityInputs(entityId: EntityId): List<InputComponent<*>>? {
        return inputs[entityId] ?: inputs[getMatchingPredictedEntityId(entityId)]
    }

    private fun getMatchingPredictedEntityId(entityId: EntityId): EntityId? {
        return idMappings[entityId]
    }

    private fun fallback(component: KClass<*>): InputComponent<*> {
        TODO()
    }

    private fun simulate() {
        for ((invoker, system) in systemInvokers) {
            logger.debug { "Running prediction simulation for system ${system::class.simpleName}" }
            (invoker as SystemInvoker<CottaSystem>).invoke(system)
        }
    }

    private fun startPredictionFrom(entities: Entities, tick: Long) {
        state.wipe()
        tickProvider.tick = tick
        if (entities is EntitiesImpl) {
            state.set(tick, entities.deepCopy())
        }
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        logger.info { "Registering ${systemClass.simpleName} for prediction simulation" }
        systemInvokers.add(invokersFactory.createInvoker(systemClass))
    }

    override fun getLocalPredictedEntities(): Collection<Entity> {
        return state.entities(tickProvider.tick).all().filter {
            it.ownedBy == Entity.OwnedBy.Player(localPlayer.playerId)
        }
    }

    override fun getPredictedEntities(): List<Entity> {
        return state.entities(tickProvider.tick).all()
            .toList()
    }
}
