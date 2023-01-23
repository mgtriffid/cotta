package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.network.protocol.CottaServerToClientPayload
import com.mgtriffid.games.cotta.server.DataToBeSentToClients

class ClientGhost {

    private val dataStorage = HashMap<Long, CottaServerToClientPayload>()

    fun send(data: DataToBeSentToClients, tick: Long) {
        val payload = constructPayload(data)
        dataStorage[tick] = payload
    }

    fun constructPayload(data: DataToBeSentToClients) : CottaServerToClientPayload {
        // serialize effects
        // serialize inputs
        // serialize entities
        // serialize deltas
        TODO()
    }
}
