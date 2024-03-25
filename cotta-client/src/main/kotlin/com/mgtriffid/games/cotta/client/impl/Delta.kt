package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.simulation.SimulationInput

sealed interface Delta {
    class Present(
        val applyDiff: (Entities) -> Unit,
        val playersDiff: List<PlayerId>,
        val input: SimulationInput,
    ) : Delta
    data object Absent : Delta
}
