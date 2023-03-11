package com.mgtriffid.games.cotta.core.serialization.impl.recipes

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.ComponentDeltaRecipe

class MapComponentDeltaRecipe(
    val componentKey: StringComponentKey, val data: Map<String, Any>
) : ComponentDeltaRecipe
