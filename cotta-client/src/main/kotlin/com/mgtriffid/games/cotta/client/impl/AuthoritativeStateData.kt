package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe

class AuthoritativeStateData<SR : StateRecipe, DR : DeltaRecipe>(
    val tick: Long,
    val state: SR,
    val deltas: List<DR>,
    val playerId: PlayerId,
    val players: Set<PlayerId>
)
