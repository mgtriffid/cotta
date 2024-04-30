package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId

interface PlayersHandler {
    fun onLeaveGame(playerId: PlayerId, entities: Entities)
}
