package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent

interface IncomingInput {
    fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>>
}
