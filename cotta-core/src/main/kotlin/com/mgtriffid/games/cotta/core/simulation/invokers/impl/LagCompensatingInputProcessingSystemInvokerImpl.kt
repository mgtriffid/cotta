package com.mgtriffid.games.cotta.core.simulation.invokers.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingInputProcessingSystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingInputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LagCompensatingInputProcessingSystemInvokerImpl @Inject constructor(
    @Named("latest") private val entities: Entities,
    private val entityOwnerSawTickProvider: EntityOwnerSawTickProvider,
    private val sawTickHolder: SawTickHolder,
    private val context: TracingInputProcessingContext,
): LagCompensatingInputProcessingSystemInvoker {
    override fun invoke(system: InputProcessingSystem) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        entities.all().forEach { process(it, system) }
    }

    private fun process(entity: Entity, system: InputProcessingSystem) {
        logger.trace { "${system::class.simpleName} processing entity ${entity.id}" }
        sawTickHolder.tick = entityOwnerSawTickProvider.getSawTickByEntity(entity)
        context.setTrace(CottaTrace.from(TraceElement.InputTraceElement(entity.id)))
        system.process(entity, context)
        context.setTrace(null)
        sawTickHolder.tick = null
    }
}
