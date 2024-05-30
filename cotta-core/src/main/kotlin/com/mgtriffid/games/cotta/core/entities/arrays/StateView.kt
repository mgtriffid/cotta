package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface StateView {
    fun getEntity(id: EntityId): Entity?
}
