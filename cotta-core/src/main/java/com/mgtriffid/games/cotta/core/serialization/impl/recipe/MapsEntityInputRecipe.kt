package com.mgtriffid.games.cotta.core.serialization.impl.recipe

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.serialization.EntityInputRecipe

class MapsEntityInputRecipe(
    override val entityId: EntityId,
    override val inputComponents: List<MapInputComponentRecipe>
) : EntityInputRecipe {
}
