package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities

interface EntityProcessingContext {
    fun fire(effect: CottaEffect)
    fun entities(): Entities // immutable entities? Yes, immutable entities.
}