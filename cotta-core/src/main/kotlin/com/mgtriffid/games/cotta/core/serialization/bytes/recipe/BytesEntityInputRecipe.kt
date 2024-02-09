package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.serialization.EntityInputRecipe

class BytesEntityInputRecipe(
    override val entityId: EntityId,
    override val inputComponents: List<BytesInputComponentRecipe>
) : EntityInputRecipe
