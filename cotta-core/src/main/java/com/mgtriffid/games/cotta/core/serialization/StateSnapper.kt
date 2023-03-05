package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entities

interface StateSnapper {
    fun snapState(entities: Entities): StateRecipe
    fun snapDelta(prev: Entities, curr: Entities): DeltaRecipe
}
