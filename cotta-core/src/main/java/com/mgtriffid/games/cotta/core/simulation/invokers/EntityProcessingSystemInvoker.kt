package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class EntityProcessingSystemInvoker(
    private val state: CottaState
) : SystemInvoker<EntityProcessingSystem> {
    override fun invoke(system: EntityProcessingSystem) {
        logger.trace { "Invoked ${system::class.qualifiedName}" }
        state.entities().all().forEach { process(it, system) }
    }

    private fun process(entity: Entity, system: EntityProcessingSystem) {
        logger.trace { "${system::class.simpleName} processing entity ${entity.id}" }
        system.process(entity)
    }
}
