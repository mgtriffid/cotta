package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext

/**
 * A system that processes entities. Each tick, each Entity is processed by each
 * [EntityProcessingSystem].
 */
interface EntityProcessingSystem : CottaSystem {
    /**
     * Process the entity. Add or remove components, change their data here.
     * Check the [Entity] for the components you need, if it doesn't have them,
     * just `return`.
     *
     * This API is definitely a subject to change.
     */
    fun process(e: Entity, ctx: EntityProcessingContext)
}
