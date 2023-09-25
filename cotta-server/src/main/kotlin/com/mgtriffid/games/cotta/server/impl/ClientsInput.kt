package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId

data class ClientsInput(
    val playersSawTicks: Map<PlayerId, Long>,
    val input: Map<EntityId, Collection<InputComponent<*>>>
)
