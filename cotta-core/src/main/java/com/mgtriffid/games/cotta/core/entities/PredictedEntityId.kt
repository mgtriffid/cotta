package com.mgtriffid.games.cotta.core.entities

data class PredictedEntityId(
    val playerId: PlayerId,
    val id: Int
) : EntityId
