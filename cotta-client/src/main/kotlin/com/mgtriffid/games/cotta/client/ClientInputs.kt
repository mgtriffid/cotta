package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.input.ClientInput

interface ClientInputs {
    fun store(input: ClientInput)
    fun get(tick: Long): ClientInput
    fun all(): Map<Long, ClientInput>
}
