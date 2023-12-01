package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.PredictedEntityId
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import java.util.ArrayList

data class ClientsInput(
    val playersSawTicks: Map<PlayerId, Long>,
    val input: Map<EntityId, Collection<InputComponent<*>>>
)

data class ClientsPredictedEntities(val createdEntities: ArrayList<Pair<CottaTrace, PredictedEntityId>>)