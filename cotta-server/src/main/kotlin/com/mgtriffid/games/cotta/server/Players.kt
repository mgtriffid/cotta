package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.PlayerId

interface Players {
    fun add(playerId: PlayerId, tick: Long)
    fun addedAtTick(tick: Long): List<PlayerId>
    fun all(): Set<PlayerId>
}
