package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.serialization.EntityRecipe

class BytesEntityRecipe(
    override val entityId: EntityId,
    override val ownedBy: Entity.OwnedBy,
    override val components: List<BytesComponentRecipe>,
): EntityRecipe
