package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem

class LagCompensatingInputProcessingSystemInvoker(
    private val state: CottaState,
    private val system: InputProcessingSystem,
    private val entityOwnerSawTickProvider: EntityOwnerSawTickProvider,
    private val sawTickHolder: InvokersFactoryImpl.SawTickHolder
): SystemInvoker {
    override fun invoke() {
        state.entities().all().forEach(::process)
    }

    private fun process(entity: Entity) {
        sawTickHolder.tick = entityOwnerSawTickProvider.getSawTickByEntityId(entityId = entity.id)
        system.update(entity)
        sawTickHolder.tick = null
    }

    interface EntityOwnerSawTickProvider {
        fun getSawTickByEntityId(entityId: EntityId): Long?
    }

}
