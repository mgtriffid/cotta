package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

sealed interface Delta {
    class Present(
        val applyDiff: (Entities) -> Unit,
        val input: SimulationInput,
        val authoritativeToPredictedEntities: Map<AuthoritativeEntityId, PredictedEntityId>,
        val tracesOfCreatedEntities : List<Pair<CottaTrace, EntityId>>,
    ) : Delta
    object Absent : Delta
}
