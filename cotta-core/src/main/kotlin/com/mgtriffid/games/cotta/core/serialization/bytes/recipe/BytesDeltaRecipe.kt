package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe

class BytesDeltaRecipe(
    override val addedEntities: List<BytesEntityRecipe>,
    override val changedEntities: List<BytesChangedEntityRecipe>,
    override val removedEntitiesIds: Set<EntityId>
) : DeltaRecipe
