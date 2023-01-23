package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.server.DataToBeSentToClients
import com.mgtriffid.games.cotta.server.ServerToClientDataChannel

class ServerToClientDataChannelImpl(
    private val tickProvider: () -> Long,
    private val clientsGhosts: ClientsGhosts
) : ServerToClientDataChannel {

    // Here we don't care much about data that comes _from_ client and about their sawTick values.
    // We do, however, care about data that is being sent _to_ clients. We record acks, we record what entities
    // did clients see and what entities they did not see. We also care about predicted entities of clients. Yes,
    // when client predicts an entity spawn, we should let it know that "ok bro we have acknowledged your prediction,
    // you predicted it as id `predicted_1`, we gave it id `543`, now we match all input you give for that entity,
    // and when we send you this entity back, your job is to start treating `543` as the id, not `predicted_1`.

    override fun send(data: DataToBeSentToClients) {
        val tick = tickProvider()
        clientsGhosts.data.forEach {
            it.value.send(data, tick)
        }
        actuallySendData(clientsGhosts, tick)
    }

    private fun actuallySendData(clientGhosts: ClientsGhosts, tick: Long) {
        // we use plugged-in serialization for now. Even though it should be different, for development we use just any
        // hacky serialization and say "developer should provide a way to serialize".
        // we also don't use deltas for now.
    }
}
