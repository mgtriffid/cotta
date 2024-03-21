package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.serialization.MetaEntitiesDeltaRecipe

class BytesMetaEntitiesDeltaRecipe(
    override val addedPlayers: List<PlayerId>,
) : MetaEntitiesDeltaRecipe