package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.input.ClientInput

interface ClientInputs {
    fun get(tick: Long): ClientInput
    fun all(): Map<Long, ClientInput>
    fun collect(entities: List<Entity>)
}
