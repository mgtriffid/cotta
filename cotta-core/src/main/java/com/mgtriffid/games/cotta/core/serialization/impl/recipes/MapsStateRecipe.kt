package com.mgtriffid.games.cotta.core.serialization.impl.recipes

import com.mgtriffid.games.cotta.core.serialization.StateRecipe

class MapsStateRecipe(
    override val entities: List<MapsEntityRecipe>
) : StateRecipe
