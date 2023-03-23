package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.network.ConnectionId
import java.util.*

private const val MAX_LAG_COMP_DEPTH_TICKS = 8

// TODO inject history length
class ClientGhost(val connectionId: ConnectionId) {

    private val logOfSentData = TreeMap<Long, KindOfData>()

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

}

interface WhatToSend {
    val necessaryData: Map<Long, KindOfData>
}

enum class KindOfData {
    STATE,
    DELTA
}
