package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.serialization.ChangedEntityRecipe

class BytesChangedEntityRecipe(
    override val entityId: EntityId,
    override val addedComponents: List<BytesComponentRecipe>,
    override val changedComponents: List<BytesComponentDeltaRecipe>,
    override val removedComponents: List<ShortComponentKey>
) : ChangedEntityRecipe
