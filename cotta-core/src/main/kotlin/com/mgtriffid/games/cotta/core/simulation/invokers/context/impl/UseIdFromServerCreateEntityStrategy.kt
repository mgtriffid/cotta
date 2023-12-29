package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import jakarta.inject.Inject
import jakarta.inject.Named

class UseIdFromServerCreateEntityStrategy @Inject constructor(
    private val registry: ServerCreatedEntitiesRegistry,
    @Named("latest") private val entities: Entities
) : CreateEntityStrategy {
    override fun createEntity(ownedBy: Entity.OwnedBy, trace: CottaTrace): Entity {
        val entityId = registry[trace]
        return entities.create(id = entityId, ownedBy = ownedBy)
    }
}
