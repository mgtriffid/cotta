package com.mgtriffid.games.cotta.experimental.guice.serialization.impl.maps.recipe

import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.EntityInputRecipe
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.InputRecipe

class MapsInputRecipe(
    private val entityInputs: List<MapsEntityInputRecipe>
) : InputRecipe {
    override fun getEntityInputs(): MutableList<EntityInputRecipe> {
        TODO("Not yet implemented")
    }

}
