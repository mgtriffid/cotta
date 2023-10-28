package com.mgtriffid.games.cotta.core.serialization.impl.recipe

import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.ComponentRecipe

class MapComponentRecipe(
    val componentKey: StringComponentKey, val data: Map<String, Any>
) : ComponentRecipe