package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.server.impl.ClientsPredictedEntities

class ServerDelta(
    val input: SimulationInput,
    val createdEntities: ClientsPredictedEntities,
)
