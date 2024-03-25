package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PredictionCreateEntityStrategy @Inject constructor(
    @Named("prediction") private val entities: Entities,
    private val predictedEntityIdGenerator: PredictedEntityIdGenerator
): CreateEntityStrategy {

    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return entities.create(predictedEntityIdGenerator.getId(), ownedBy)
    }
}
