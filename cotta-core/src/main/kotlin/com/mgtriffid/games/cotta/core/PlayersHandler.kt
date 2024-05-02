package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId

interface PlayersHandler {
    fun onEnterGame(playerId: PlayerId, entities: Entities) {}
    fun onLeaveGame(playerId: PlayerId, entities: Entities) {}
}
