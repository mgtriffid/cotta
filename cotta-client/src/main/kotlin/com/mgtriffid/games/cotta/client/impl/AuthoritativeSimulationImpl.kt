package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.AuthoritativeSimulation
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.InputProcessing
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

class AuthoritativeSimulationImpl @Inject constructor(
    @Named("simulation") private val state: CottaState,
    private val tick: TickProvider,
    @Named("simulation") private val invokersFactory: InvokersFactory,
    private val effectBus: EffectBus,
    private val playersSawTicks: PlayersSawTicks,
    private val inputProcessing: InputProcessing,
) : AuthoritativeSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>() // TODO pathetic casts

    override fun tick(input: SimulationInput) {
        effectBus.clear()
        state.advance(tick.tick)
        tick.tick++
        putInputIntoEntities(input)
        processInput(input)
        fillPlayersSawTicks(input)
        simulate()
    }

    private fun processInput(input: SimulationInput) {
        inputProcessing.process(input, state.entities(tick.tick), effectBus)
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
        systemInvokers.add(invokersFactory.createInvoker(systemClass))
    }

    private fun putInputIntoEntities(input: SimulationInput) {
        getEntitiesWithInputComponents().forEach { e ->
            logger.debug { "Entity ${e.id} has some input components:" }
            e.inputComponents().forEach { c ->
                val component = input.inputForEntityAndComponent(e.id, c)
                logger.debug { "  $component" }
                e.setInputComponent(c, component)
            }
        }
    }

    private fun getEntitiesWithInputComponents() = state.entities(tick.tick).all().filter {
        it.hasInputComponents()
    }
}
