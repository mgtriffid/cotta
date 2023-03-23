package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.network.ConnectionId
import java.util.*

private const val MAX_LAG_COMP_DEPTH_TICKS = 8

// TODO inject history length
class ClientGhost(
    val connectionId: ConnectionId
) {
    private val logOfSentData = HashSet<WhatToSend.WhatToSendItem>()

    fun whatToSend(tick: Long): WhatToSend {
        val lastKnownToClient = lastKnownToClient()

        return if (tick - lastKnownToClient > MAX_LAG_COMP_DEPTH_TICKS) {
            object : WhatToSend {
                override val necessaryData = listOf(
                    WhatToSend.WhatToSendItem(tick - MAX_LAG_COMP_DEPTH_TICKS, KindOfData.STATE)) +
                        ((tick - MAX_LAG_COMP_DEPTH_TICKS + 1)..tick).map {
                            WhatToSend.WhatToSendItem(it, KindOfData.DELTA)
                        }
            }
        } else {
            object : WhatToSend {
                override val necessaryData = ((lastKnownToClient + 1)..(tick)).map {
                    WhatToSend.WhatToSendItem(it, KindOfData.DELTA)
                }
            }
        }.also {
            logOfSentData.addAll(it.necessaryData)
            logOfSentData.removeAll { it.tick < tick - 128 }
        }
    }

    private fun lastKnownToClient(): Long {
        return logOfSentData.filter { when (it.kindOfData) {
            KindOfData.DELTA, KindOfData.STATE -> true
            KindOfData.CLIENT_META_ENTITY_ID -> false
        } }.maxOfOrNull { it.tick } ?: -1
    }
}

interface WhatToSend {
    val necessaryData: List<WhatToSendItem>

    data class WhatToSendItem(
        val tick: Long,
        val kindOfData: KindOfData
    )
}

enum class KindOfData {
    STATE,
    DELTA,
    CLIENT_META_ENTITY_ID
}
