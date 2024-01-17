package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId

// TODO provide ability to show authoritative versions of entities together with predicted so that user could debug
interface DrawableState {
    object EMPTY : DrawableState {
        override val entities: List<Entity> = emptyList()
        override val authoritativeToPredictedEntityIds: Map<AuthoritativeEntityId, PredictedEntityId> = emptyMap()
    }

    val entities: List<Entity>
    val authoritativeToPredictedEntityIds: Map<AuthoritativeEntityId, PredictedEntityId>
}
