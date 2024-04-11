package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.simulation.PlayersDiff

class SimulationInputData(
    val tick: Long,
    val playersSawTicks: Map<PlayerId, Long>,
    val playersInputs: Map<PlayerId, PlayerInput>,
    val playersDiff: PlayersDiff,
    val idSequence: Int,
    val confirmedClientInput: ClientInputId
)
