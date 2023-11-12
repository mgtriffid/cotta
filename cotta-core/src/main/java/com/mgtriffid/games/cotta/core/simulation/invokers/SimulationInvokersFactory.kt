package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.annotations.LagCompensated
import com.mgtriffid.games.cotta.core.simulation.invokers.impl.EntityProcessingSystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.impl.LagCompensatingEffectsConsumerInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.impl.SimpleEffectsConsumerSystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import jakarta.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

class SimulationInvokersFactory @Inject constructor(
    private val lagCompensatingEffectsConsumerInvoker: LagCompensatingEffectsConsumerInvoker,
    private val simpleEffectsConsumerSystemInvoker: SimpleEffectsConsumerSystemInvoker,
    private val entityProcessingSystemInvoker: EntityProcessingSystemInvoker,
    private val lagCompensatingInputProcessingSystemInvoker: LagCompensatingInputProcessingSystemInvoker
) : InvokersFactory {
    // simulation invoker! very specific thing
    override fun <T : CottaSystem> createInvoker(systemClass: KClass<T>): Pair<SystemInvoker<*>, T> {
        val ctor = systemClass.getConstructor()
        val system = ctor.call()
        return when (system) {
            is InputProcessingSystem -> {
                // propagates sawTick to lagCompensatingEffectBus so that effect would know what was seen by the player
                Pair(lagCompensatingInputProcessingSystemInvoker, system)
            }

            is EntityProcessingSystem -> {
                // normal stuff, uses LatestEntities and lagCompensatingEffectBus (why not normal tho)
                Pair(entityProcessingSystemInvoker, system)
            }

            is EffectsConsumerSystem -> if (
                systemClass.hasAnnotation<LagCompensated>()
            ) {
                Pair(lagCompensatingEffectsConsumerInvoker, system)
            } else {
                Pair(simpleEffectsConsumerSystemInvoker, system)
            }

            else -> { throw IllegalStateException("Unexpected implementation of CottaSystem") }
        }
    }
}
