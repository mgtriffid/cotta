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
    @Named("simulation") private val tick: TickProvider,
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
        processInput(input)
        logger.debug { input.inputForPlayers() }
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
}
