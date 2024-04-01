package com.mgtriffid.games.cotta.server.impl

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
    private val clientTickCursor = ClientTickCursor()

    fun whatToSend(): WhatToSend {
        return if (stateSent) {
            WhatToSend.SIMULATION_INPUTS
        } else {
            stateSent = true
            WhatToSend.STATE
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

    class ClientTickCursor {
        var lastUsedInput = -1L
        var state = State.AWAITING_INPUTS

        enum class State {
            AWAITING_INPUTS,
            RUNNING
        }
    }
}

enum class WhatToSend {
    STATE,
    SIMULATION_INPUTS
}
