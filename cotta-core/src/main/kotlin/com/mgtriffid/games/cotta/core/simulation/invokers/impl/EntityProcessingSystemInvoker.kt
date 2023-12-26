package com.mgtriffid.games.cotta.core.simulation.invokers.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class EntityProcessingSystemInvoker @Inject constructor(
    @Named("latest") private val entities: Entities,
    private val context: EntityProcessingContext
) : SystemInvoker<EntityProcessingSystem> {
    override fun invoke(system: EntityProcessingSystem) {
        logger.trace { "Invoked ${system::class.qualifiedName}" }
        entities.all().forEach { process(it, system) }
    }

    private fun process(entity: Entity, system: EntityProcessingSystem) {
        logger.trace { "${system::class.simpleName} processing entity ${entity.id}" }
        system.process(entity, context)
    }
}
