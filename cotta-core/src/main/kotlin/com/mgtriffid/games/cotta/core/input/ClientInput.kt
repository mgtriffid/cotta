package com.mgtriffid.games.cotta.core.input

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent

interface ClientInput {
    val inputs: Map<EntityId, List<InputComponent<*>>>
}
