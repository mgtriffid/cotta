package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

data class ClientsInput(
    val playersSawTicks: Map<PlayerId, Long>,
    val input: Map<EntityId, Collection<InputComponent<*>>>,
    val inputForPlayers: Map<PlayerId, PlayerInput>
)

data class ClientsPredictedEntities(val createdEntities: ArrayList<Pair<CottaTrace, PredictedEntityId>>)
