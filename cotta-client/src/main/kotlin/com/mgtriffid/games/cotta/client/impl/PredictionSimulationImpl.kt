package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeToPredictedEntityIdMappings
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesImpl
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
    @Named("prediction") override val effectBus: EffectBus,
    @Named("prediction") private val tickProvider: TickProvider,
    @Named("localInput") private val localInputTickProvider: TickProvider,
    private val localPlayer: LocalPlayer,
) : PredictionSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>()

    override fun predict(initialEntities: Entities, ticks: List<Long>, authoritativeTick: Long) {
        val lag = authoritativeTick - ticks.first()
        startPredictionFrom(initialEntities, authoritativeTick)
        run(ticks, lag)
    }

    private fun startPredictionFrom(entities: Entities, tick: Long) {
        state.wipe()
        tickProvider.tick = tick
        if (entities is EntitiesImpl) {
            state.set(tick, entities.deepCopy())
        }
    }

    private fun run(ticks: List<Long>, lag: Long) {
        logger.debug { "Running prediction simulation for ticks $ticks" }
        for (tick in ticks) {
            localInputTickProvider.tick = tick
            logger.debug { "Running prediction simulation for tick $tick" }
            effectBus.clear()
            logger.debug { "Advancing state: to tick ${tickProvider.tick}" }
            state.advance(tickProvider.tick)
            tickProvider.tick++

            simulate()
        }
    }

    private fun simulate() {
        for ((invoker, system) in systemInvokers) {
            logger.debug { "Running prediction simulation for system ${system::class.simpleName}" }
            (invoker as SystemInvoker<CottaSystem>).invoke(system)
        }
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        logger.info { "Registering ${systemClass.simpleName} for prediction simulation" }
        systemInvokers.add(invokersFactory.createInvoker(systemClass))
    }

    override fun getLocalPredictedEntities(): Collection<Entity> {
        return state.entities(tickProvider.tick).dynamic().filter {
            it.ownedBy == Entity.OwnedBy.Player(localPlayer.playerId)
        }
    }

    override fun getPreviousLocalPredictedEntities(): List<Entity> {
        if (tickProvider.tick == 0L) {
            return emptyList()
        }
        return state.entities(tickProvider.tick - 1).dynamic().filter {
            it.ownedBy == Entity.OwnedBy.Player(localPlayer.playerId)
        }
    }
}
