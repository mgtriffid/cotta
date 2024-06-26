package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity

interface EffectProcessingContext {
    fun fire(effect: CottaEffect)

    fun entities(): Entities // mutable entities

    fun createEntity(ownedBy: Entity.OwnedBy): Entity

    fun createEntity() = createEntity(Entity.OwnedBy.System)

    fun clock(): CottaClock
}
