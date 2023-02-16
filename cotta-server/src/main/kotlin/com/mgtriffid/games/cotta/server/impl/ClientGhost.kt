package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.network.protocol.CottaServerToClientPayload
import com.mgtriffid.games.cotta.network.protocol.serialization.Delta
import com.mgtriffid.games.cotta.network.protocol.serialization.ServerToClientGameDataPacket
import com.mgtriffid.games.cotta.network.protocol.serialization.StateSnapshot
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.utils.drain
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val DELTAS_BUFFER_LENGTH = 128
// TODO inject history length
class ClientGhost(val connectionId: ConnectionId) {

    private var stateKnownToClient: Long? = null
    private val queueToSend = ConcurrentLinkedQueue<ServerToClientGameDataPacket>()

    fun send(data: DataForClients, tick: Long) {
        sendHistoricalDataIfNeeded(data, tick)
        sendDelta(data, tick)
    }

    private fun sendHistoricalDataIfNeeded(data: DataForClients, tick: Long) {
        sendState(data.entities(tick - 7), tick - 7)
        for (t in (tick - 6) until tick) {
            sendDelta(data, t)
        }
    }

    private fun sendDelta(data: DataForClients, tick: Long) {
        val curr = data.entities(tick).all()
        val prev = data.entities(tick - 1).all()
        // for these we only use ids
        val removedEntitiesIds = prev.map { it.id }.toSet() - curr.map { it.id }.toSet()
        // here we use ids and all components, or maybe a more efficient way to send, like if it's a blueprint then
        // blueprint id will work fine
        val addedEntities = curr.filter { c -> prev.none { p -> p.id == c.id } }
        // and for these we calculate precisely components that were changed in at least one field
        val changedEntities = curr.filter { c -> prev.any { p -> p.id == c.id } }
        val delta = Delta(removedEntitiesIds, addedEntities, changedEntities, tick)
        queueToSend.add(ServerToClientGameDataPacket.DeltaPacket(delta, tick))
    }

    private fun sendState(entities: Entities, tick: Long) {
        queueToSend.add(
            ServerToClientGameDataPacket.StatePacket(
                stateSnapshot = StateSnapshot(entities = entities.all().toSet()),
                tick = tick
            )
        )
    }

    private fun stateKnownToClientIsObsolete() : Boolean {
        return stateKnownToClient == null
    }

    fun drainQueue() = queueToSend.drain()

    private enum class WhatWasSent {
        STATE,
        DELTA
    }
}
