package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.impl.EntitiesInternal

class CreatingStaticEntities(val impl: EntitiesInternal) : Entities by impl {
    private var id = 0
    // TODO ownedBy does not make sense here
    override fun create(ownedBy: Entity.OwnedBy): Entity {
        return impl.create(ownedBy)
    }
}
