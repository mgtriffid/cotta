package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientSimulation
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.impl.EffectsHistoryImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import jdk.jfr.Name
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class ClientSimulationImpl @Inject constructor(
    private val state: CottaState,
    private val tickProvider: TickProvider,
    @Named("historyLength") private val historyLength: Int,
    private val simulationInputHolder: SimulationInputHolder
) : ClientSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, *>>() // TODO pathetic casts

    // TODO null object? lateinit var? tf is wrong with this
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
        val input = simulationInputHolder.get()
        state.entities().all().filter {
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
}
