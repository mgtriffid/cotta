package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.TickProvider

interface CottaClient {
    fun initialize()

    fun tick()
    fun getPredictedEntities(): List<Entity>

    // TODO better place or better beans. This is here now only for drawing. Incorrect.
    val state: CottaState
    val tickProvider: TickProvider
}