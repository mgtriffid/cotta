package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext

interface EntityProcessingSystem : CottaSystem {
    fun process(e: Entity, ctx: EntityProcessingContext)
}
