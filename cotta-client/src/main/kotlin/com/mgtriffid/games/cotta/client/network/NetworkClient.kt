package com.mgtriffid.games.cotta.client.network

import com.mgtriffid.games.cotta.client.impl.AuthoritativeState
import com.mgtriffid.games.cotta.client.impl.Delta
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface NetworkClient {
    fun fetch()
    fun connect()
    fun send(createdEntities: List<Pair<CottaTrace, EntityId>>, tick: Long)
    fun tryGetDelta(tick: Long): Delta
    fun tryGetAuthoritativeState(): AuthoritativeState
    fun deltaAvailable(tick: Long): Boolean
    fun send(input: PlayerInput, currentTick: Long)
}
