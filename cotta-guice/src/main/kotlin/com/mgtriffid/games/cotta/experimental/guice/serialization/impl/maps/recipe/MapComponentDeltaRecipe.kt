package com.mgtriffid.games.cotta.experimental.guice.serialization.impl.maps.recipe

import com.mgtriffid.games.cotta.experimental.guice.data.ComponentKey
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ComponentDeltaRecipe

class MapComponentDeltaRecipe(
    val componentKey: ComponentKey.StringComponentKey, val data: Map<String, Any>
) : ComponentDeltaRecipe
