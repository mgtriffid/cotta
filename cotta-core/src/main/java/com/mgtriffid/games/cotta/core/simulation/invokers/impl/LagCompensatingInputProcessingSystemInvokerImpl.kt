package com.mgtriffid.games.cotta.core.simulation.invokers.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingInputProcessingSystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LagCompensatingInputProcessingSystemInvokerImpl @Inject constructor(
    @Named("latest") private val entities: Entities,
    private val entityOwnerSawTickProvider: EntityOwnerSawTickProvider,
    private val sawTickHolder: SawTickHolder,
    private val context: InputProcessingContext
): LagCompensatingInputProcessingSystemInvoker {
    override fun invoke(system: InputProcessingSystem) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        entities.all().forEach { process(it, system) }
    }

    private fun process(entity: Entity, system: InputProcessingSystem) {
        logger.debug { "${system::class.simpleName} processing entity ${entity.id}" }
        sawTickHolder.tick = entityOwnerSawTickProvider.getSawTickByEntity(entity)
        system.process(entity, context)
        sawTickHolder.tick = null
    }

}