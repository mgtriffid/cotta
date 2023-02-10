package com.mgtriffid.games.cotta.network.protocol.serialization

import com.mgtriffid.games.cotta.core.entities.Entity

data class Delta(
    val removedEntitiesIds: Set<Int>,
    val addedEntities: List<Entity>,
    val changedEntities: List<Entity>,
    val tick: Long
)
