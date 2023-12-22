package com.mgtriffid.games.cotta.client.invokers

import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactory
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.getConstructor
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import jakarta.inject.Inject
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class PredictionInvokersFactory @Inject constructor(
    private val predictedInputProcessingSystemInvoker: PredictedInputProcessingSystemInvoker,
    private val effectsConsumerSystemInvoker: PredictionEffectsConsumerSystemInvoker,
) : InvokersFactory {
    override fun <T : CottaSystem> createInvoker(systemClass: KClass<T>): Pair<SystemInvoker<*>, CottaSystem> {
        val ctor = systemClass.getConstructor()
        val system = ctor.call()
        return when (system) {
            is InputProcessingSystem -> {
                Pair(predictedInputProcessingSystemInvoker, system)
            }

            is EffectsConsumerSystem -> {
                Pair(effectsConsumerSystemInvoker, system)
            }

            else -> {
                throw IllegalStateException("Unexpected implementation of CottaSystem")
            }
        }
    }
}
