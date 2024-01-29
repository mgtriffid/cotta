package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity

interface InputProcessingContext {
    fun clock(): CottaClock
    fun fire(effect: CottaEffect)
    fun entities(): Entities // immutable entities
}
