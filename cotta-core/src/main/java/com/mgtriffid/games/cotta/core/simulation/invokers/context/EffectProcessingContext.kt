package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity

interface EffectProcessingContext {
    fun fire(effect: CottaEffect)

    fun entities(): List<Entity> // mutable entities

    fun createEntity(): Entity
}