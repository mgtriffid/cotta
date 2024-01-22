package com.mgtriffid.games.cotta.core.input

import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface ClientInput {
    val inputs: Map<EntityId, List<InputComponent<*>>>
}
