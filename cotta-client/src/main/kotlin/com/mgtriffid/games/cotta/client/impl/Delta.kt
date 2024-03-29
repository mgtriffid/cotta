package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.SimulationInput

class Delta(
    val playersDiff: List<PlayerId>,
    val input: SimulationInput,
)
