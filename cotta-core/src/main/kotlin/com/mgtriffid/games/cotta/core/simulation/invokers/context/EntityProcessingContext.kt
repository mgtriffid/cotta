package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities

/**
 * Provides access to things that may be needed. First and foremost, the actual
 * state, the ability to publish Effects.
 */
interface EntityProcessingContext {
    /**
     * Returns a [CottaClock] that should be used as a primary source of time.
     */
    fun clock(): CottaClock

    /**
     * Fires a [CottaEffect] to be later consumed by some
     * [com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem].
     */
    fun fire(effect: CottaEffect)

    /**
     * Returns [Entities], meaning the current state of the simulation.
     */
    fun entities(): Entities
}
