package com.mgtriffid.games.cotta.experimental.guice.serialization.impl.maps.recipe

import com.mgtriffid.games.cotta.experimental.guice.data.ComponentKey
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ChangedEntityRecipe
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ComponentDeltaRecipe
import com.mgtriffid.games.cotta.experimental.guice.serialization.recipe.ComponentRecipe


class MapsChangedEntityRecipe(
    private val entityId: Int,
    private val changedComponents: List<MapComponentDeltaRecipe>,
    private val addedComponents: List<MapComponentRecipe>,
    private val removedComponents: List<ComponentKey.StringComponentKey>
) : ChangedEntityRecipe {
    override fun getEntityId(): Int {
        TODO("Not yet implemented")
    }

    override fun getChangedComponents(): MutableList<ComponentDeltaRecipe> {
        TODO("Not yet implemented")
    }

    override fun getAddedComponents(): MutableList<ComponentRecipe> {
        TODO("Not yet implemented")
    }

    override fun getRemovedComponents(): MutableList<ComponentKey> {
        TODO("Not yet implemented")
    }
}
