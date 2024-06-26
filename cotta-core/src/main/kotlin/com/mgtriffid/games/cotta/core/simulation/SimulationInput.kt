package com.mgtriffid.games.cotta.core.simulation

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput

interface SimulationInput {
    fun inputForPlayers(): Map<PlayerId, PlayerInput>

    fun nonPlayerInput(): NonPlayerInput = NonPlayerInput.Blank

    fun playersSawTicks(): Map<PlayerId, Long>
    fun playersDiff(): PlayersDiff
}

data class PlayersDiff(
    val added: Set<PlayerId>,
    val removed: Set<PlayerId>
) {
    companion object {
        val Empty = PlayersDiff(emptySet(), emptySet())
    }
}
