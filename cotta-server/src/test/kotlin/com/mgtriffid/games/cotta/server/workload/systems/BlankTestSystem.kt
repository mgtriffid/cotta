package com.mgtriffid.games.cotta.server.workload.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingCottaSystem

class BlankTestSystem(var counter: Int): EntityProcessingCottaSystem {
    override fun update(e: Entity) {
        counter++
    }
}
