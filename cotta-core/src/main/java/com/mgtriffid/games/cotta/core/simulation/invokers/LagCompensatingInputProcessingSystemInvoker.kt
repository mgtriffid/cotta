package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LagCompensatingInputProcessingSystemInvoker(
    private val state: CottaState,
    private val system: InputProcessingSystem,
    private val entityOwnerSawTickProvider: EntityOwnerSawTickProvider,
    private val sawTickHolder: InvokersFactoryImpl.SawTickHolder
): SystemInvoker {
    override fun invoke() {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        state.entities().all().forEach(::process)
    }

    private fun process(entity: Entity) {
        logger.debug { "${system::class.simpleName} processing entity ${entity.id}" }
        sawTickHolder.tick = entityOwnerSawTickProvider.getSawTickByEntity(entity)
        system.process(entity)
        sawTickHolder.tick = null
    }

    interface EntityOwnerSawTickProvider {
        fun getSawTickByEntity(entity: Entity): Long?
    }

}
