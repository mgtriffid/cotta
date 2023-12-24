package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.simulation.SimulationInput

sealed interface Delta {
    class Present(
        val applyDiff: (Entities) -> Unit,
        val input: SimulationInput
    ) : Delta
    object Absent : Delta
}
