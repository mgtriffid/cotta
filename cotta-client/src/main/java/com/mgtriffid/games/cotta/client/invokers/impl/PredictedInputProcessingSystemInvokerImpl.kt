package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.client.invokers.PredictedInputProcessingSystemInvoker
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import jakarta.inject.Inject
import jakarta.inject.Named

class PredictedInputProcessingSystemInvokerImpl @Inject constructor(
    @Named("predictedLocal") private val entities: Entities, // TODO only for reading
    @Named("predicted") private val context: InputProcessingContext,
): PredictedInputProcessingSystemInvoker {
    override fun invoke(system: InputProcessingSystem) {
        entities.all().forEach { process(it, system) }
    }

    private fun process(entity: Entity, system: InputProcessingSystem) {
        system.process(entity, context)
    }
}