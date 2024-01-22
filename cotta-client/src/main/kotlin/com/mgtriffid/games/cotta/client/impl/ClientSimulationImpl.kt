package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientSimulation
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class ClientSimulationImpl @Inject constructor(
    @Named("simulation") private val state: CottaState,
    private val tick: TickProvider,
    @Named("simulation") private val invokersFactory: InvokersFactory,
    private val effectBus: EffectBus,
    private val playersSawTicks: PlayersSawTicks
) : ClientSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>() // TODO pathetic casts

    override fun tick(input: SimulationInput) {
        effectBus.clear()
        state.advance(tick.tick)
        tick.tick++
        putInputIntoEntities(input)
        fillPlayersSawTicks(input)
        simulate()
    }

    private fun fillPlayersSawTicks(input: SimulationInput) {
        playersSawTicks.set(input.playersSawTicks())
    }

    private fun simulate() {
        for ((invoker, system) in systemInvokers) {
            (invoker as SystemInvoker<CottaSystem>).invoke(system) // TODO cast issue
        }
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        logger.info { "Registering system '${systemClass.simpleName}'" }
        systemInvokers.add(invokersFactory.createInvoker(systemClass))
    }

    private fun putInputIntoEntities(input: SimulationInput) {
        getEntitiesWithInputComponents().forEach { e ->
            logger.trace { "Entity ${e.id} has some input components:" }
            e.inputComponents().forEach { c ->
                val component = input.inputForEntityAndComponent(e.id, c)
                logger.trace { "  $component" }
                e.setInputComponent(c, component)
            }
        }
    }

    private fun getEntitiesWithInputComponents() = state.entities(tick.tick).all().filter {
        it.hasInputComponents()
    }
}
