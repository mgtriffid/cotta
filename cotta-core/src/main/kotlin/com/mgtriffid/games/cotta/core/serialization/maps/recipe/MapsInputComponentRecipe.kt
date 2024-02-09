package com.mgtriffid.games.cotta.core.serialization.maps.recipe

import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.InputComponentRecipe

class MapsInputComponentRecipe(
    val componentKey: StringComponentKey,
    val data: Map<String, Any>
): InputComponentRecipe
