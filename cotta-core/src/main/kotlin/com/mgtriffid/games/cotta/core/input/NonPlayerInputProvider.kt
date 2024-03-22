package com.mgtriffid.games.cotta.core.input

import com.mgtriffid.games.cotta.core.entities.Entities

/**
 * Provides input for the server-controlled Entities, be it NPCs or scripted events or whatever.
 */
interface NonPlayerInputProvider {
    fun input(entities: Entities): NonPlayerInput
}
