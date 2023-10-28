package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity

interface InputProcessingContext {
    fun fire(effect: CottaEffect)
    fun entities(): List<Entity> // immutable entities
}