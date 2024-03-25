package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CreateAndRecordCreateEntityStrategy @Inject constructor(
    @Named("latest") private val entities: Entities
) : CreateEntityStrategy {
    // TODO introduce concept of _circumstances_.
    //  Circumstances being? Like, what's the tick we saw and who we are and what was the effect?
    //  now, if we want to track down the creation of entity on Server then we also need to track sawTick in some kind
    //  of context, and who was the player - too.
    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        val entity = entities.create(ownedBy)
        return entity
    }
}
