package com.mgtriffid.games.cotta.core.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext

interface InputProcessingSystem: CottaSystem {
    fun process(e: Entity, ctx: InputProcessingContext)
}
