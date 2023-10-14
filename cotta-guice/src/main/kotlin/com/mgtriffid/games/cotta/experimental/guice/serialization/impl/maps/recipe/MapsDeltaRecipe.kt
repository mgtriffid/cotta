package com.mgtriffid.games.cotta.experimental.guice.serialization.impl.maps.recipe

import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ChangedEntityRecipe
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.DeltaRecipe
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.EntityRecipe

class MapsDeltaRecipe(
    private val addedEntities: List<MapsEntityRecipe>,
    private val changedEntities: List<MapsChangedEntityRecipe>,
    private val removedEntitiesIds: Set<Int>
) : DeltaRecipe {
    override fun getAddedEntities(): MutableList<EntityRecipe> {
        TODO("Not yet implemented")
    }

    override fun getChangedEntities(): MutableList<ChangedEntityRecipe> {
        TODO("Not yet implemented")
    }

    override fun getRemovedEntitiesIds(): MutableList<Int> {
        TODO("Not yet implemented")
    }

}
