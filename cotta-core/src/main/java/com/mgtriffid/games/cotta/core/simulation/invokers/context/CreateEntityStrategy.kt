package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.EffectHolder

interface CreateEntityStrategy {
    fun createEntity(ownedBy: Entity.OwnedBy, effectHolder: EffectHolder): Entity
}