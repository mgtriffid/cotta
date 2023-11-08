package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.entities.Entity

interface CreateEntityStrategy {
    fun createEntity(ownedBy: Entity.OwnedBy): Entity
}