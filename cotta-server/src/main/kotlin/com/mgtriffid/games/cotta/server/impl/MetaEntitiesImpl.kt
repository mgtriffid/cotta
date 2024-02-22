package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.server.MetaEntities
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

class MetaEntitiesImpl : MetaEntities {
    private val data = HashMap<PlayerId, EntityId>()
    private val newlyAdded = TreeMap<Long, List<Pair<EntityId, PlayerId>>>()
    override fun get(playerId: PlayerId): EntityId = data[playerId]
        ?: throw IllegalStateException("No meta entity for player ${playerId.id}")

    override fun set(playerId: PlayerId, entityId: EntityId) {
        data[playerId] = entityId
    }

    override fun recordNew(newEntities: List<Pair<EntityId, PlayerId>>, tick: Long) {
        newlyAdded[tick] = newEntities
        cleanUp(newlyAdded, tick)
    }

    override fun addedAtTick(tick: Long): List<Pair<EntityId, PlayerId>> {
        return newlyAdded[tick] ?: emptyList()
    }

    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            data.remove(it)
        }
    }
}
