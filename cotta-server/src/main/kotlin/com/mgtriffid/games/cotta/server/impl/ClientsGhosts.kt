package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.network.ConnectionId
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ClientsGhosts<IR: InputRecipe> {

    val playerByConnection = HashMap<ConnectionId, PlayerId>()
    val data = HashMap<PlayerId, ClientGhost>()

    // TODO handle removing ghost
    fun addGhost(playerId: PlayerId, connectionId: ConnectionId) {
        data[playerId] = ClientGhost(connectionId)
        playerByConnection[connectionId] = playerId
    }

    fun removeGhost(playerId: PlayerId) {
        val ghost = data.remove(playerId)
        if (ghost == null) {
            logger.warn { "There's no ghost for player $playerId for some reason" }
            return
        }
        playerByConnection.remove(ghost.connectionId)
    }
}
