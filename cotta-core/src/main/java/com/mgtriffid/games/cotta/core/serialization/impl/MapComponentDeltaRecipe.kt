package com.mgtriffid.games.cotta.core.serialization.impl

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.registry.ComponentKey
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.ComponentDeltaRecipe

class MapComponentDeltaRecipe<C : Component<C>>(
    val componentKey: StringComponentKey, val data: Map<String, Any>
) : ComponentDeltaRecipe<C>
