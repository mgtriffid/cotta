package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.PlayerInput

class SimulationInputData(
    val tick: Long,
    val playersSawTicks: Map<PlayerId, Long>,
    val playersInputs: Map<PlayerId, PlayerInput>,
    val playersDiff: PlayersDiff,
    val idSequence: Int,
) {
    data class PlayersDiff(
        val added: Set<PlayerId>,
    )
}
