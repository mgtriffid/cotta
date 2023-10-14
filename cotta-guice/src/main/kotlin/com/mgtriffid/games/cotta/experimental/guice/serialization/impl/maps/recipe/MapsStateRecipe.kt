package com.mgtriffid.games.cotta.experimental.guice.serialization.impl.maps.recipe

import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.EntityRecipe
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.StateRecipe

class MapsStateRecipe(
    private val entities: List<MapsEntityRecipe>
) : StateRecipe {
    override fun getEntities(): List<EntityRecipe> {
        return entities;
    }
}
