package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent

/**
 * Provides input for the server-controlled Entities, be it NPCs or scripted events or whatever.
 */
interface NonPlayerInputProvider {
    fun input(entities: Entities): Map<EntityId, Collection<InputComponent<*>>>
}
