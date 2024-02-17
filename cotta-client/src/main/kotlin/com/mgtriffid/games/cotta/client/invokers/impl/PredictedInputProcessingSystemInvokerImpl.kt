package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.client.impl.LocalPlayer
import com.mgtriffid.games.cotta.client.invokers.PredictedInputProcessingSystemInvoker
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.SawTickHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingInputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PredictedInputProcessingSystemInvokerImpl @Inject constructor(
    @Named("prediction") private val entities: Entities, // TODO only for reading
    private val sawTickHolder: SawTickHolder,
    @Named("prediction") private val context: TracingInputProcessingContext,
    private val localPlayer: LocalPlayer,
): PredictedInputProcessingSystemInvoker {
    override fun invoke(system: InputProcessingSystem) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        entities.all()
            .filter { it.shouldBePredicted() }
            .forEach { process(it, system) }
    }

    private fun Entity.shouldBePredicted() = ownedBy == Entity.OwnedBy.Player(localPlayer.playerId)

    private fun process(entity: Entity, system: InputProcessingSystem) {
        logger.trace { "${system::class.simpleName} processing entity ${entity.id}" }
        context.setTrace(CottaTrace.from(TraceElement.InputTraceElement(entity.id)))
        system.process(entity, context)
        context.setTrace(null)
        sawTickHolder.tick = null
    }
}
