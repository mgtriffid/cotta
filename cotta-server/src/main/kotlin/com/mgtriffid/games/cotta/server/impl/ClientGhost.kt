package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.MAX_LAG_COMP_DEPTH_TICKS
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.network.ConnectionId
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

private const val HISTORY_LENGTH = 128

// TODO inject history length
class ClientGhost(
    val connectionId: ConnectionId
) {
    private var stateSent = false
    private var lastUsedIncomingInput: PlayerInput? = null
    private val recordsOfSentData = RecordsOfSentData()
    private val clientTickCursor = ClientTickCursor()

    fun whatToSend(tick: Long): WhatToSend {
        return recordsOfSentData.whatToSend(tick)
    }

    fun whatToSend2(): WhatToSend2 {
        return if (stateSent) {
            WhatToSend2.SIMULATION_INPUTS
        } else {
            stateSent = true
            WhatToSend2.STATE
        }
    }

    fun tickCursorState(): ClientTickCursor.State {
        return clientTickCursor.state
    }

    fun setCursorState(state: ClientTickCursor.State) {
        clientTickCursor.state = state
    }

    fun setLastUsedTick(tick: Long) {
        clientTickCursor.lastUsedInput = tick
    }

    fun lastUsedInput(): Long {
        return clientTickCursor.lastUsedInput
    }

    fun setLastUsedIncomingInput(playerInput: PlayerInput) {
        this.lastUsedIncomingInput = playerInput
    }

    fun getLastUsedIncomingInput(): PlayerInput {
        return lastUsedIncomingInput ?: throw IllegalStateException("No last used input")
    }

    inner class RecordsOfSentData {
        private val logOfSentData = HashSet<WhatToSend.WhatToSendItem>()
        private var metaEntitySent = false  // TODO acknowledged mb? That's more accurate.

        fun whatToSend(tick: Long): WhatToSend {
            val lastKnownToClient = lastKnownToClient()
            val necessaryData = ArrayList<WhatToSend.WhatToSendItem>()
            if (tick - lastKnownToClient > MAX_LAG_COMP_DEPTH_TICKS) {
                necessaryData.addAll(listOf(
                    WhatToSend.WhatToSendItem(tick - MAX_LAG_COMP_DEPTH_TICKS, KindOfData.STATE)
                ) + ((tick - MAX_LAG_COMP_DEPTH_TICKS + 1)..tick).map {
                    WhatToSend.WhatToSendItem(it, KindOfData.DELTA)
                })
            } else {
                necessaryData.addAll(((lastKnownToClient + 1)..(tick)).map {
                    WhatToSend.WhatToSendItem(it, KindOfData.DELTA)
                })
            }
            if (!metaEntitySent) {
                logger.debug { "Need to send meta entity id to $connectionId" }
                necessaryData.add(WhatToSend.WhatToSendItem(tick, KindOfData.PLAYER_ID))
                metaEntitySent = true
            }
            return object : WhatToSend {
                override val necessaryData = necessaryData
            }.also { whatToSend ->
                logOfSentData.addAll(whatToSend.necessaryData)
                logOfSentData.removeAll { it.tick < tick - HISTORY_LENGTH }
                logger.debug { "What to send to connection $connectionId: $necessaryData" }
            }
        }

        private fun lastKnownToClient(): Long {
            return logOfSentData.filter { when (it.kindOfData) {
                KindOfData.DELTA, KindOfData.STATE -> true
                KindOfData.PLAYER_ID -> false
            } }.maxOfOrNull { it.tick } ?: -1
        }
    }

    class ClientTickCursor {
        var lastUsedInput = -1L
        var state = State.AWAITING_INPUTS

        enum class State {
            AWAITING_INPUTS,
            RUNNING
        }
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
    PLAYER_ID,
}

enum class WhatToSend2 {
    STATE,
    SIMULATION_INPUTS
}
