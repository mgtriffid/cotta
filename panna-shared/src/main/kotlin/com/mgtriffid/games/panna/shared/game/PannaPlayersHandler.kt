package com.mgtriffid.games.panna.shared.game

import com.mgtriffid.games.cotta.core.PlayersHandler
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent

class PannaPlayersHandler : PlayersHandler {

    override fun onEnterGame(playerId: PlayerId, entities: Entities) {

    }

    override fun onLeaveGame(playerId: PlayerId, entities: Entities) {
        val entitiesToRemove = entities.all().filter {
            it.hasComponent(SteamManPlayerComponent::class) && it.ownedBy == Entity.OwnedBy.Player(playerId)
        }.map { it.id }
        entitiesToRemove.forEach { entities.remove(it) }
    }
}
