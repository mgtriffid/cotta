package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.ClientSimulation
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import jakarta.inject.Inject
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class ClientSimulationImpl @Inject constructor(
    private val state: CottaState,
    private val simulationInputHolder: SimulationInputHolder,
    private val invokersFactory: InvokersFactory,
    private val effectBus: EffectBus
) : ClientSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>() // TODO pathetic casts

    override fun tick() {
        effectBus.clear()
        state.advance()
        putInputIntoEntities()
        simulate()
        predict()
    }

    private fun simulate() {
        for ((invoker, system) in systemInvokers) {
            (invoker as SystemInvoker<CottaSystem>).invoke(system) // TODO cast issue
        }
    }

    private fun predict() {
        // Article on Unity says we need to distinguish OwnerPredicted and just Predicted. Which kind of makes sense.
        // My avatar is OwnerPredicted. My missiles are OwnerPredicted but also Predicted for my enemies.
        // There has to be a strategy that selects Entities that should be processed on this particular Client.
        // What about Effects though?
        // So for Movement Input we process Input, we fire Effect to actually move, then we process this Effect. All three
        // should happen. Let's say if collision happens then there's some effect on collision and damage. In that case
        // we should track effect of collision, track effect of damage intent but not use actual damage processing. Which
        // means we can't really do that automatic. It is up to developer to specify up until which point simulation runs.

        // copy state to predicted state
        // invoke systems but only on part of those entities
        //      create new invokers
        //      create new Entities that refer to predicted state
        //
        // actually invoke many times because we have certain list of inputs
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        logger.info { "Registering system '${systemClass.simpleName}'" }
        systemInvokers.add(invokersFactory.createInvoker(systemClass))
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
