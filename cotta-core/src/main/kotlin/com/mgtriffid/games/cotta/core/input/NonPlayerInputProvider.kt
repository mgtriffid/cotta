package com.mgtriffid.games.cotta.core.input

import com.mgtriffid.games.cotta.core.entities.Entities

/**
 * Provides input for the server-controlled Entities, be it NPCs or scripted
 * events or anything like that.
 */
interface NonPlayerInputProvider {
    fun input(entities: Entities): NonPlayerInput
}

object NoOpNonPlayerInputProvider : NonPlayerInputProvider {
    override fun input(entities: Entities): NonPlayerInput = NonPlayerInput.Blank
}
