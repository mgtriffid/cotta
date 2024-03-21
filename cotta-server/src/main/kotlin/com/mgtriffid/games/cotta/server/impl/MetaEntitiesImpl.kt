package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.server.Players
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

class MetaEntitiesImpl : Players {
    private val newlyAdded = TreeMap<Long, List<PlayerId>>()

    override fun recordNew(newPlayers: List<PlayerId>, tick: Long) {
        newlyAdded[tick] = newPlayers
        cleanUp(newlyAdded, tick)
    }

    override fun addedAtTick(tick: Long): List<PlayerId> {
        return newlyAdded[tick] ?: emptyList()
    }

    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            data.remove(it)
        }
    }
}
