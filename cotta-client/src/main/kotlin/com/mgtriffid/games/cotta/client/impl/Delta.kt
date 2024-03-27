package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.SimulationInput

sealed interface Delta {
    class Present(
        val playersDiff: List<PlayerId>,
        val input: SimulationInput,
    ) : Delta
    data object Absent : Delta
}
