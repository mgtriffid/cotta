package com.mgtriffid.games.cotta.core.simulation.invokers.context

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.serialization.impl.dto.EntityOwnedByDto

interface EffectProcessingContext {
    fun fire(effect: CottaEffect)

    fun entities(): Entities // mutable entities TODO change to return something that can't create Entities

    fun createEntity(ownedBy: Entity.OwnedBy): Entity
}
