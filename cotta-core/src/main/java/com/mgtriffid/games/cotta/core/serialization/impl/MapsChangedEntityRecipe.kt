package com.mgtriffid.games.cotta.core.serialization.impl

import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.ChangedEntityRecipe

class MapsChangedEntityRecipe(
    override val entityId: Int,
    override val changedComponents: List<MapComponentDeltaRecipe<*>>,
    override val addedComponents: List<MapComponentRecipe<*>>,
    override val removedComponents: List<StringComponentKey>
) : ChangedEntityRecipe
