package com.mgtriffid.games.cotta.core.serialization.maps.recipe

import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.ComponentRecipe

class MapsComponentRecipe(
    val componentKey: StringComponentKey, val data: Map<String, Any>
) : ComponentRecipe
