package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.LocalPlayerInputs
import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesImpl
import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// TODO test this, without unit tests it's super fragile
class PredictionSimulationImpl @Inject constructor(
    @Named("prediction") private val invokersFactory: InvokersFactory,
    @Named("prediction") private val state: CottaState,
    @Named("prediction") override val effectBus: EffectBus,
    @Named("prediction") private val tickProvider: TickProvider,
    private val inputs: LocalPlayerInputs,
    private val inputProcessing: InputProcessing,
    private val localPlayer: LocalPlayer,
) : PredictionSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>()

    override fun predict(initialEntities: Entities, lastConfirmedInputId: ClientInputId, authoritativeTick: Long) {
        val unconfirmedInputs =
            inputs.all().keys.filter { it.id > lastConfirmedInputId.id }
                .also { logger.debug { it.joinToString() } } // TODO explicit sorting
        startPredictionFrom(initialEntities, authoritativeTick)
        run(unconfirmedInputs)
    }

    private fun startPredictionFrom(entities: Entities, tick: Long) {
        state.wipe()
        tickProvider.tick = tick
        if (entities is EntitiesImpl) {
            state.set(tick, entities.deepCopy())
        }
    }

    private fun run(inputIds: List<ClientInputId>) {
        logger.debug { "Running prediction using inputs $inputs" }
        for (inputId in inputIds) {
            logger.debug { "Running prediction simulation for input $inputId" }
            effectBus.clear()
            logger.debug { "Advancing state: to tick ${tickProvider.tick}" }
            state.advance(tickProvider.tick)
            tickProvider.tick++
            inputProcessing.processPlayerInput(localPlayer.playerId, inputs.get(inputId), state.entities(tickProvider.tick), effectBus)
            simulate()
        }
    }

    private fun simulate() {
        for ((invoker, system) in systemInvokers) {
            logger.debug { "Running prediction simulation for system ${system::class.simpleName}" }
            (invoker as SystemInvoker<CottaSystem>).invoke(system)
        }
    }

    override fun registerSystem(system: CottaSystem) {
        logger.info { "Registering ${system::class.simpleName} for prediction simulation" }
        systemInvokers.add(invokersFactory.createInvoker(system))
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
