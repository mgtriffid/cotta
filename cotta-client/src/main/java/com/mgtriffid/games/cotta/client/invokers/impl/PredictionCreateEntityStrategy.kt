package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import jakarta.inject.Inject
import jakarta.inject.Named

class PredictionCreateEntityStrategy @Inject constructor(
    @Named("prediction") private val entities: Entities,
    private val registry: PredictedCreatedEntitiesRegistry,
    @Named("prediction") private val tickProvider: TickProvider,
    private val predictedEntityIdGenerator: PredictedEntityIdGenerator
): CreateEntityStrategy {

    // IF this prediction happens for the first time
    // - we create a new Entity, generate an id (predicted), record trace to id
    // IF this prediction already happened before, if we have trace there
    // - we create entity wth that exact id
    override fun createEntity(ownedBy: Entity.OwnedBy, trace: CottaTrace): Entity {
        val entityId = registry.find(trace, tickProvider.tick)
        return if (entityId != null) {
            entities.createEntity(entityId, ownedBy)
        } else {
            val newEntity = entities.createEntity(predictedEntityIdGenerator.getId(), ownedBy)
            registry.record(trace, tickProvider.tick, newEntity.id)
            newEntity
        }
    }
}
