package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface CreateEntityStrategy {
    fun createEntity(ownedBy: Entity.OwnedBy, trace: CottaTrace): Entity
}