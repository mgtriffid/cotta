package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientSimulation
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.impl.EffectsHistoryImpl
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.invokers.HistoricalLagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactoryImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBusImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class ClientSimulationImpl(
    private val state: CottaState,
    private val tickProvider: TickProvider,
    private val historyLength: Int
) : ClientSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, *>>() // TODO pathetic casts

    // TODO null object? lateinit var? tf is wrong with this
    private var inputForUpcomingTick: SimulationInput = object : SimulationInput {
        override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> = emptyMap()
        override fun playersSawTicks(): Map<PlayerId, Long> = emptyMap()
    }
    private val playersSawTicks = object: PlayersSawTicks {
        override fun get(playerId: PlayerId): Long? {
            return inputForUpcomingTick.playersSawTicks()[playerId]
        }
    }
    private val effectBus = EffectBus.getInstance()
    private val effectsHistory = EffectsHistoryImpl(historyLength = historyLength)

    /*private val invokersFactory: InvokersFactory = run {
        val sawTickHolder = InvokersFactoryImpl.SawTickHolder(null)
        InvokersFactory.getInstance(
            HistoricalLagCompensatingEffectBus(
                history = effectsHistory,
                impl = LagCompensatingEffectBusImpl(effectBus, sawTickHolder),
                tickProvider = tickProvider
            ),
            state,
            playersSawTicks,
            sawTickHolder
        )
    }*/

    override fun setInputForUpcomingTick(input: SimulationInput) {
        inputForUpcomingTick = input
    }

    override fun tick() {
        effectBus.clear()
        state.advance()
        putInputIntoEntities()
        for (invoker in systemInvokers) {
//            invoker()
        }
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        logger.info { "Registering system '${systemClass.simpleName}'" }
//        systemInvokers.add(invokersFactory.createInvoker(systemClass))
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
