package com.mgtriffid.games.cotta.core.serialization.impl.recipes

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.serialization.EntityRecipe

class MapsEntityRecipe(
    override val entityId: EntityId, override val components: List<MapComponentRecipe>
) : EntityRecipe
