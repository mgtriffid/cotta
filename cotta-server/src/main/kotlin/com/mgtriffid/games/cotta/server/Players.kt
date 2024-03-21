package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface Players {
    fun recordNew(newEntities: List<PlayerId>, tick: Long)
    fun addedAtTick(tick: Long): List<PlayerId>
}
