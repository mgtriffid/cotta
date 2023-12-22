package com.mgtriffid.games.cotta.core.serialization.impl.recipe

import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.InputComponentRecipe

class MapInputComponentRecipe(
    val componentKey: StringComponentKey,
    val data: Map<String, Any>
): InputComponentRecipe
