package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.network.ConnectionId
import com.mgtriffid.games.cotta.core.serialization.Delta
import com.mgtriffid.games.cotta.core.serialization.ServerToClientGameDataPiece
import com.mgtriffid.games.cotta.core.serialization.StateSnapshot
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.utils.drain
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

private const val MAX_LAG_COMP_DEPTH_TICKS = 8

// TODO inject history length
class ClientGhost(val connectionId: ConnectionId) {

    private var stateKnownToClient: Long? = null
    private val queueToSend = ConcurrentLinkedQueue<ServerToClientGameDataPiece>()
    private val logOfSentData = TreeMap<Long, KindOfData>()

    fun send(data: DataForClients, tick: Long) {
        sendHistoricalDataIfNeeded(data, tick)
        sendDelta(data, tick)
    }

    // 20
    fun whatToSend(tick: Long): WhatToSend {
        // 15
        val lastKnownToClient = lastKnownToClient()

        return if (tick - lastKnownToClient > MAX_LAG_COMP_DEPTH_TICKS) {
            object : WhatToSend {
                override val necessaryData: Map<Long, KindOfData>
                    get() = mapOf((tick - MAX_LAG_COMP_DEPTH_TICKS) to KindOfData.STATE) +
                            ((tick - MAX_LAG_COMP_DEPTH_TICKS + 1)..tick).associateWith { KindOfData.DELTA }

            }
        } else {
            object : WhatToSend {
                override val necessaryData = ((lastKnownToClient + 1)..(tick)).associateWith {
                    KindOfData.DELTA
                }
            }
        }.also {
            logOfSentData.putAll(it.necessaryData)
            val size = logOfSentData.size
            if (size > 128) {
                repeat(size - 128) {
                    logOfSentData.remove(logOfSentData.firstKey())
                }
            }
        }
    }

    private fun lastKnownToClient(): Long {
        return logOfSentData.takeIf { it.isNotEmpty() }?.lastKey() ?: -1
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
        queueToSend.add(ServerToClientGameDataPiece.DeltaPiece(tick, delta))
    }

    private fun sendState(entities: Entities, tick: Long) {
        queueToSend.add(
            ServerToClientGameDataPiece.StatePiece(
                tick = tick,
                stateSnapshot = StateSnapshot(entities = entities.all().toSet())
            )
        )
    }

    private fun stateKnownToClientIsObsolete(): Boolean {
        return stateKnownToClient == null
    }

    fun drainQueue() = queueToSend.drain()
}

interface WhatToSend {
    val necessaryData: Map<Long, KindOfData>
}

enum class KindOfData {
    STATE,
    DELTA
}
