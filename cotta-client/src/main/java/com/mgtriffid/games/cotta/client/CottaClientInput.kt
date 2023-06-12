package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent

interface CottaClientInput {
    fun input(entity: Entity, metaEntityId: EntityId): List<InputComponent<*>>
}
