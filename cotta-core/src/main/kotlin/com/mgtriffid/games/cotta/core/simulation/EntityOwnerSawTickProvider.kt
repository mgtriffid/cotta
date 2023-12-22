package com.mgtriffid.games.cotta.core.simulation

import com.mgtriffid.games.cotta.core.entities.Entity

interface EntityOwnerSawTickProvider {
    fun getSawTickByEntity(entity: Entity): Long?
}