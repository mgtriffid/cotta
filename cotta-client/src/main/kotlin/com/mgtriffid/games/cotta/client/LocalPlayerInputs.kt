package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.input.ClientInputId
import com.mgtriffid.games.cotta.core.input.PlayerInput

interface LocalPlayerInputs {
    fun get(clientInputId: ClientInputId): PlayerInput
    fun all(): Map<ClientInputId, PlayerInput>
    fun collect()
    fun unsent(): List<Triple<ClientInputId, PlayerInput, Long>>
}
