package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.network.ConnectionId
import mu.KotlinLogging
import java.util.*
import java.util.logging.Logger

private const val MAX_LAG_COMP_DEPTH_TICKS = 8

private val logger = KotlinLogging.logger {}

// TODO inject history length
class ClientGhost(
    val connectionId: ConnectionId
) {
    private val logOfSentData = HashSet<WhatToSend.WhatToSendItem>()
    private var metaEntitySent = false

    fun whatToSend(tick: Long): WhatToSend {
        val lastKnownToClient = lastKnownToClient()
        val necessaryData = ArrayList<WhatToSend.WhatToSendItem>()
        if (tick - lastKnownToClient > MAX_LAG_COMP_DEPTH_TICKS) {
            necessaryData.addAll(listOf(
                WhatToSend.WhatToSendItem(tick - MAX_LAG_COMP_DEPTH_TICKS, KindOfData.STATE)
            ) + ((tick - MAX_LAG_COMP_DEPTH_TICKS + 1)..tick).map {
                        WhatToSend.WhatToSendItem(it, KindOfData.DELTA)
                    }
            )
        } else {
            necessaryData.addAll(((lastKnownToClient + 1)..(tick)).map {
                WhatToSend.WhatToSendItem(it, KindOfData.DELTA)
            })
        }
        if (!metaEntitySent) {
            logger.debug { "Need to send meta entity id to $connectionId" }
            necessaryData.add(WhatToSend.WhatToSendItem(tick, KindOfData.CLIENT_META_ENTITY_ID))
            metaEntitySent = true
        }
        return object : WhatToSend {
            override val necessaryData = necessaryData
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
