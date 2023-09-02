package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class EntityProcessingSystemInvoker(
    private val state: CottaState,
    private val system: EntityProcessingSystem
) : SystemInvoker {
    override fun invoke() {
        logger.trace { "Invoked ${system::class.qualifiedName}" }
        state.entities().all().forEach(::process)
    }

    private fun process(entity: Entity) {
        logger.trace { "${system::class.simpleName} processing entity ${entity.id}" }
        system.process(entity)
    }
}
