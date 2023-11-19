package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.EffectHolder
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreatedEntities
import com.mgtriffid.games.cotta.core.simulation.invokers.context.SimplestCreateEntityTrace
import jakarta.inject.Inject
import jakarta.inject.Named

class CreateAndRecordCreateEntityStrategy @Inject constructor(
    @Named("latest") private val entities: Entities,
    private val createdEntities: CreatedEntities
) : CreateEntityStrategy {
    // where do I record mapping of predictedId to authoritative id?
    // when creating entity need to know predictedId if it was predicted
    // so client needs to send all predictedIds to server together with circumstances those Entities were created.
    // Circumstances being? Like, what's the tick we saw and who we are and what was the effect?
    // now, if we want to track down the creation of entity on Server then we also need to track sawTick in some kind
    // of context, and who was the player - too.
    override fun createEntity(ownedBy: Entity.OwnedBy, effectHolder: EffectHolder): Entity {
        val entity = entities.createEntity(ownedBy)
        createdEntities.record(
            createEntityTrace = SimplestCreateEntityTrace(ownedBy),
            entityId = entity.id
        )
        return entity
    }
}
