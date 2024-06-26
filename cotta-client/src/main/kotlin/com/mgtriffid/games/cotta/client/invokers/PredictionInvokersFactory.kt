package com.mgtriffid.games.cotta.client.invokers

import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.getConstructor
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import jakarta.inject.Inject
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class PredictionInvokersFactory @Inject constructor(
    private val effectsConsumerInvoker: PredictionEffectsConsumerSystemInvoker,
    private val entityProcessingInvoker: PredictionEntityProcessingSystemInvoker,
) : InvokersFactory {
    override fun createInvoker(system: CottaSystem): Pair<SystemInvoker<*>, CottaSystem> {
        return when (system) {

            is EffectsConsumerSystem<*> -> {
                Pair(effectsConsumerInvoker, system)
            }

            is EntityProcessingSystem -> {
                Pair(entityProcessingInvoker, system)
            }

            else -> { throw IllegalStateException("Unexpected implementation of CottaSystem which is actually sealed") }
        }
    }
}
