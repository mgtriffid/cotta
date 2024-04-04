package com.mgtriffid.games.cotta.core.simulation.impl

import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.simulation.Simulation
import com.mgtriffid.games.cotta.core.simulation.Players
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
    @Named(SIMULATION) private val simulationTick: TickProvider,
    @Named("simulation") private val invokersFactory: InvokersFactory,
    private val effectBus: EffectBus,
    private val playersSawTicks: PlayersSawTicks,
    private val inputProcessing: InputProcessing,
    private val players: Players
) : Simulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>() // TODO pathetic casts

    override fun tick(input: SimulationInput) {
        effectBus.clear()
        state.advance(simulationTick.tick)
        simulationTick.tick++
        processInput(input)
        logger.debug { input.inputForPlayers() }
        fillPlayersSawTicks(input)
        simulate()
        processPlayersDiff(input)
    }

    private fun processInput(input: SimulationInput) {
        inputProcessing.process(
            input,
            state.entities(simulationTick.tick),
            effectBus
        )
    }

    private fun processPlayersDiff(input: SimulationInput) {
        input.playersDiff().added.forEach { playerId ->
            players.add(playerId, simulationTick.tick)
        }
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
