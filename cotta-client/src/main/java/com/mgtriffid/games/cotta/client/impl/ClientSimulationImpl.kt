package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientSimulation
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.invokers.HistoricalLagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactoryImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBusImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ClientSimulationImpl(
    val state: CottaState,
    val tickProvider: TickProvider,
    val historyLength: Int
) : ClientSimulation {
    private val systemInvokers = ArrayList<SystemInvoker>()

    private val playersSawTicks = HashMap<PlayerId, Long>()

    private var inputForUpcomingTick: SimulationInput = object : SimulationInput {
        override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> = emptyMap()
    }
    private val effectBus = EffectBus.getInstance()
    private val effectsHistory = EffectsHistory(historyLength = historyLength)

    private val invokersFactory: InvokersFactory = run {
        val sawTickHolder = InvokersFactoryImpl.SawTickHolder(null)
        InvokersFactory.getInstance(
            HistoricalLagCompensatingEffectBus(
                history = effectsHistory,
                impl = LagCompensatingEffectBusImpl(effectBus, sawTickHolder),
                tickProvider = tickProvider
            ),
            state,
            playersSawTicks,
            tickProvider,
            sawTickHolder
        )
    }

    override fun setInputForUpcomingTick(input: SimulationInput) {
        this.inputForUpcomingTick = input
    }

    override fun tick() {
        effectBus.clear()
        state.advance()
        // here we don't have that processEnterGameIntents() call like on server side
        // that is because we don't process entering game here, for those entities we just transfer them
        // from server to client and apply them as part of delta
        putInputIntoEntities()
        for (invoker in systemInvokers) {
            invoker()
        }
    }

    private fun putInputIntoEntities() {
        state.entities().all().filter {
            it.hasInputComponents()
        }.forEach { e ->
            logger.trace { "Entity ${e.id} has some input components:" }
            e.inputComponents().forEach { c ->
                val component = inputForUpcomingTick.inputForEntityAndComponent(e.id, c)
                logger.trace { "  $component" }
                e.setInputComponent(c, component)
            }
        }
    }
}
