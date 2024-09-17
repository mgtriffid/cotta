package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem

class BlankTestSystem: EntityProcessingSystem {
    companion object {
        var counter: Int = 0
    }

    override fun process(e: Entity, ctx: EntityProcessingContext) {
        counter++
    }
}
