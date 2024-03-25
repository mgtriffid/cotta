package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.PlayerInput

data class ClientsInput(
    val playersSawTicks: Map<PlayerId, Long>,
    val inputForPlayers: Map<PlayerId, PlayerInput>
)
