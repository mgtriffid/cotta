package com.mgtriffid.games.cotta.experimental.guice.serialization.impl.maps.recipe

import com.mgtriffid.games.cotta.experimental.guice.data.ComponentKey
import com.mgtriffid.games.cotta.experimental.guice.data.OwnedBy
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ComponentRecipe
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.EntityRecipe

class MapsEntityRecipe(
    private val entityId: Int,
    private val ownedBy: OwnedBy,
    private val components: List<MapComponentRecipe>,
    private val inputComponents: List<ComponentKey.StringComponentKey>
) : EntityRecipe {
    override fun getEntityId(): Int {
        return entityId
    }

    override fun getOwnedBy(): OwnedBy {
        return ownedBy
    }

    override fun getComponents(): List<ComponentRecipe> {
        return components
    }

    override fun getInputComponents(): List<ComponentKey> {
        return inputComponents
    }
}
