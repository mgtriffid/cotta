package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.PredictionSimulation
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class PredictionSimulationImpl @Inject constructor(
    @Named("prediction") private val invokersFactory: InvokersFactory
) : PredictionSimulation {
    private val systemInvokers = ArrayList<Pair<SystemInvoker<*>, CottaSystem>>()

    override fun tick() {
        logger.info { "PredictionSimulation#tick" }
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        systemInvokers.add(invokersFactory.createInvoker(systemClass))
    }
}
