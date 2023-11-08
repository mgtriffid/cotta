package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.SimplestCreateEntityTrace
import jakarta.inject.Inject
import jakarta.inject.Named

class UseIdFromServerCreateEntityStrategy @Inject constructor(
    private val registry: ServerCreatedEntitiesRegistry,
    @Named("latest") private val entities: Entities
) : CreateEntityStrategy {
    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        val entityId = registry[SimplestCreateEntityTrace(ownedBy)]
        return entities.createEntity(id = entityId, ownedBy = ownedBy)
    }
}
