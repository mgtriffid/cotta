package com.mgtriffid.games.cotta.experimental.guice.serialization.impl.maps.recipe

import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.EntityInputRecipe
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.InputComponentRecipe

class MapsEntityInputRecipe(
    private val entityId: Int,
    private val inputComponents: List<MapInputComponentRecipe>
) : EntityInputRecipe {
    override fun getEntityId(): Int {
        TODO("Not yet implemented")
    }

    override fun getInputComponents(): MutableList<InputComponentRecipe> {
        TODO("Not yet implemented")
    }

}
