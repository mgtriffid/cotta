package com.mgtriffid.games.cotta.core.serialization.impl

import com.mgtriffid.games.cotta.core.serialization.EntityRecipe

class MapsEntityRecipe(
    override val entityId: Int, override val components: List<MapComponentRecipe<*>>
) : EntityRecipe