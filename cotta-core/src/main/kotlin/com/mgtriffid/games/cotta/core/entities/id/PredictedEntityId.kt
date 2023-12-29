package com.mgtriffid.games.cotta.core.entities.id

import com.mgtriffid.games.cotta.core.entities.PlayerId

data class PredictedEntityId(
    val playerId: PlayerId,
    val id: Int
) : EntityId
