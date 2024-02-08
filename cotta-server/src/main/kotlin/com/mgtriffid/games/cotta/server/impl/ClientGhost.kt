package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsInputRecipe
import com.mgtriffid.games.cotta.network.ConnectionId
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

private const val HISTORY_LENGTH = 128
private const val MAX_LAG_COMP_DEPTH_TICKS = 8

// TODO inject history length
class ClientGhost(
    val connectionId: ConnectionId
) {
    private val recordsOfSentData = RecordsOfSentData()
    private val clientTickCursor = ClientTickCursor()
    private var lastUserIncomingInput: MapsInputRecipe? = null

    fun whatToSend(tick: Long): WhatToSend {
        return recordsOfSentData.whatToSend(tick)
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

    fun setLastUsedIncomingInput(mapsInputRecipe: MapsInputRecipe) {
        this.lastUserIncomingInput = mapsInputRecipe
    }

    fun getLastUsedIncomingInput(): MapsInputRecipe {
        return lastUserIncomingInput ?: throw IllegalStateException("No last used input")
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
                necessaryData.add(WhatToSend.WhatToSendItem(tick, KindOfData.CLIENT_META_ENTITY_ID))
                metaEntitySent = true
            }
            return object : WhatToSend {
                override val necessaryData = necessaryData
            }.also { whatToSend ->
                logOfSentData.addAll(whatToSend.necessaryData)
                logOfSentData.removeAll { it.tick < tick - HISTORY_LENGTH }
            }
        }

        private fun lastKnownToClient(): Long {
            return logOfSentData.filter { when (it.kindOfData) {
                KindOfData.DELTA, KindOfData.STATE -> true
                KindOfData.CLIENT_META_ENTITY_ID -> false
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
    CLIENT_META_ENTITY_ID
}
