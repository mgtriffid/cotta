package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.simulation.invokers.impl.EntityProcessingSystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.impl.LagCompensatingEffectsConsumerInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.impl.SimpleEffectsConsumerSystemInvoker
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.cotta.core.systems.LagCompensatedEffectsConsumerSystem
import jakarta.inject.Inject

class SimulationInvokersFactory @Inject constructor(
    private val lagCompensatingEffectsConsumerInvoker: LagCompensatingEffectsConsumerInvoker,
    private val simpleEffectsConsumerSystemInvoker: SimpleEffectsConsumerSystemInvoker,
    private val entityProcessingSystemInvoker: EntityProcessingSystemInvoker,
) : InvokersFactory {

    override fun createInvoker(system: CottaSystem): Pair<SystemInvoker<*>, CottaSystem> {
        return when (system) {
            is EntityProcessingSystem -> {
                // normal stuff, uses LatestEntities and lagCompensatingEffectBus (why not normal tho)
                Pair(entityProcessingSystemInvoker, system)
            }

            is EffectsConsumerSystem<*> -> if (
                system is LagCompensatedEffectsConsumerSystem<*>
            ) {
                Pair(lagCompensatingEffectsConsumerInvoker, system)
            } else {
                Pair(simpleEffectsConsumerSystemInvoker, system)
            }

            else -> { throw IllegalStateException("Unexpected implementation of CottaSystem") }
        }
    }
}
