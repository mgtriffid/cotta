package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext

// TODO not sure if even needed
class SimpleEffectProcessingContext : EffectProcessingContext {
    override fun fire(effect: CottaEffect) {
        TODO("Not yet implemented")
    }

    override fun entities(): Entities {
        TODO("Not yet implemented")
    }

    override fun createEntity(): Entity {
        TODO("Not yet implemented")
    }
}