package com.mgtriffid.games.cotta.core.simulation.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.Players
import java.util.*
import kotlin.math.min

class PlayersImpl : Players {
    private val newlyAdded = TreeMap<Long, MutableList<PlayerId>>()
    private val newlyRemoved = TreeMap<Long, MutableList<PlayerId>>()
    private val players = mutableSetOf<PlayerId>()

    override fun add(playerId: PlayerId, tick: Long) {
        newlyAdded.computeIfAbsent(tick) { _ -> ArrayList() }.add(playerId)
        cleanUp(newlyAdded, tick)
        players.add(playerId)
    }

    override fun remove(playerId: PlayerId, tick: Long) {
        newlyRemoved.computeIfAbsent(tick) { _ -> ArrayList() }.add(playerId)
        cleanUp(newlyRemoved, tick)
        players.remove(playerId)
    }

    override fun all(): Set<PlayerId> {
        return players
    }

    override fun addedAtTick(tick: Long): List<PlayerId> {
        return newlyAdded[tick] ?: emptyList()
    }

    override fun removedAtTick(tick: Long): List<PlayerId> {
        return newlyRemoved[tick] ?: emptyList()
    }

    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            data.remove(it)
        }
    }
}
