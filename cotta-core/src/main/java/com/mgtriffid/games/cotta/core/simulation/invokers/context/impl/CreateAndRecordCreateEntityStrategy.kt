package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreatedEntities
import com.mgtriffid.games.cotta.core.simulation.invokers.context.SimplestCreateEntityTrace
import jakarta.inject.Inject
import jakarta.inject.Named

class CreateAndRecordCreateEntityStrategy @Inject constructor(
    @Named("latest") private val entities: Entities,
    private val createdEntities: CreatedEntities
) : CreateEntityStrategy {
    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        val entity = entities.createEntity(ownedBy)
        createdEntities.record(
            createEntityTrace = SimplestCreateEntityTrace(ownedBy),
            entityId = entity.id
        )
        return entity
    }
}
