package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent

interface ServerInputProvider {
    fun input(entities: Entities): Map<EntityId, Collection<InputComponent<*>>>
}
