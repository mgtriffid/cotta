package com.mgtriffid.games.cotta.core.serialization.impl

import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe

class MapsDeltaRecipe(
    override val addedEntities: List<MapsEntityRecipe>,
    override val changedEntities: List<MapsChangedEntityRecipe>,
    override val removedEntitiesIds: Set<Int>
) : DeltaRecipe
