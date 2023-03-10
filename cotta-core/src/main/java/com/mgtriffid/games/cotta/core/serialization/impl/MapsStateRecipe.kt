package com.mgtriffid.games.cotta.core.serialization.impl

import com.mgtriffid.games.cotta.core.serialization.EntityRecipe
import com.mgtriffid.games.cotta.core.serialization.StateRecipe

class MapsStateRecipe(
    override val entities: List<EntityRecipe>
) : StateRecipe
