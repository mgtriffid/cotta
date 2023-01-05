package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem

class EntityProcessingSystemInvoker(
    private val state: CottaState,
    private val system: EntityProcessingSystem
) : SystemInvoker {
    override fun invoke() {
        state.entities().all().forEach(::process)    }

    private fun process(entity: Entity) {
        system.update(entity)
    }
}
