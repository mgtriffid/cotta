package com.mgtriffid.games.cotta.core.serialization.impl.recipe

import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.ComponentDeltaRecipe

class MapComponentDeltaRecipe(
    val componentKey: StringComponentKey, val data: Map<String, Any>
) : ComponentDeltaRecipe
