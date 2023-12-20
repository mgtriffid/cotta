package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreatedEntities
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.server.EntitiesCreatedOnClientsRegistry
import com.mgtriffid.games.cotta.server.PredictedToAuthoritativeIdMappings
import jakarta.inject.Inject
import jakarta.inject.Named

class CreateAndRecordCreateEntityStrategy @Inject constructor(
    @Named("latest") private val entities: Entities,
    private val createdEntities: CreatedEntities,
    private val entitiesCreatedOnClientsRegistry: EntitiesCreatedOnClientsRegistry,
    private val predictedToAuthoritativeIdMappings: PredictedToAuthoritativeIdMappings
) : CreateEntityStrategy {
    // TODO introduce concept of _circumstances_.
    //  Circumstances being? Like, what's the tick we saw and who we are and what was the effect?
    //  now, if we want to track down the creation of entity on Server then we also need to track sawTick in some kind
    //  of context, and who was the player - too.
    override fun createEntity(ownedBy: Entity.OwnedBy, trace: CottaTrace): Entity {
        val entity = entities.createEntity(ownedBy)
        val predictedEntityWithSimilarTrace = entitiesCreatedOnClientsRegistry.find(trace)
        if (predictedEntityWithSimilarTrace != null) {
            // GROOM express that the second argument here is an AuthoritativeEntityId
            predictedToAuthoritativeIdMappings.record(predictedEntityWithSimilarTrace, entity.id)
        }
        createdEntities.record(
            trace = trace,
            entityId = entity.id
        )
        return entity
    }
}
