package com.mgtriffid.games.cotta.core.entities.arrays.storage

import com.mgtriffid.games.cotta.core.entities.Entity

internal class EntityData(
    val components: EntityComponents,
    val ownedBy: Entity.OwnedBy
) {
}
