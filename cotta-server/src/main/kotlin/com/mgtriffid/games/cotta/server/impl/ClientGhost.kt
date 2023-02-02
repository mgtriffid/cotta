package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.network.protocol.CottaServerToClientPayload
import com.mgtriffid.games.cotta.server.ComponentDeltas
import com.mgtriffid.games.cotta.server.DataForClients

class ClientGhost {

    private val dataStorage = HashMap<Long, CottaServerToClientPayload>()
    private var stateKnownToClient: Long? = null
    private val sentToClient: List<Pair<Long, WhatWasSent>> = ArrayList() // TODO replace with a ringbuffer


    fun send(data: DataForClients, tick: Long) {
        TODO()
    }

    private fun sendDelta(deltas: ComponentDeltas) {
        TODO("Not yet implemented")
    }

    private fun sendState(entities: Entities) {
        TODO("Not yet implemented")
    }

    private fun stateKnownToClientIsObsolete() : Boolean {
        return stateKnownToClient == null
    }

    private enum class WhatWasSent {
        STATE,
        DELTA
    }
}
