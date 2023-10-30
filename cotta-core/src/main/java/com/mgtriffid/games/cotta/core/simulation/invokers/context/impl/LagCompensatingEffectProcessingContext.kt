package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.impl.EntityOwnerSawTickProviderImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactoryImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingEffectBus
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import jakarta.inject.Inject
import jakarta.inject.Named

class LagCompensatingEffectProcessingContext @Inject constructor(
    @Named("lagCompensated") private val lagCompensatingEffectBus: LagCompensatingEffectBus,
    private val state: CottaState,
    private val sawTickHolder: InvokersFactoryImpl.SawTickHolder
) : EffectProcessingContext {
    override fun fire(effect: CottaEffect) {
        lagCompensatingEffectBus.publisher().fire(effect)
    }

    override fun entities(): Entities {
        return InvokersFactoryImpl.ReadingFromPreviousTickEntities(state = state, sawTickHolder = sawTickHolder)

    }

    override fun createEntity(): Entity {
        TODO("Not yet implemented")
    }
}