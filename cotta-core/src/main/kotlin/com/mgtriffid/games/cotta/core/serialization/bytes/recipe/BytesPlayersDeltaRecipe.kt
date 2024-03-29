package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.serialization.PlayersDeltaRecipe

class BytesPlayersDeltaRecipe(
    override val addedPlayers: List<PlayerId>,
) : PlayersDeltaRecipe
