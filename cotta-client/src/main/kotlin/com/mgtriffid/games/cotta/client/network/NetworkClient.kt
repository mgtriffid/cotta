package com.mgtriffid.games.cotta.client.network

import com.mgtriffid.games.cotta.client.impl.AuthoritativeState
import com.mgtriffid.games.cotta.client.impl.Delta
import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.input.PlayerInput

interface NetworkClient {
    fun fetch()
    fun initialize()
    fun tryGetDelta(tick: Long): Delta?
    fun tryGetAuthoritativeState(): AuthoritativeState
    fun deltaAvailable(tick: Long): Boolean
    fun send(inputId: ClientInputId, input: PlayerInput, sawTick: Long)
    fun enterGame()
    fun disconnect()
}
