package com.mgtriffid.games.cotta.core.serialization.impl

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.registry.ComponentKey
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.ComponentRecipe

class MapComponentRecipe<C : Component<C>>(
    val componentKey: StringComponentKey, val data: Map<String, Any>
) : ComponentRecipe<C>
