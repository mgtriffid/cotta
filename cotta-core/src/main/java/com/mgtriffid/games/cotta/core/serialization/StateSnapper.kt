package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entities

interface StateSnapper<SR: StateRecipe, DR: DeltaRecipe> {
    fun snapState(entities: Entities): SR
    fun snapDelta(prev: Entities, curr: Entities): DR
    fun unpackStateRecipe(entities: Entities, recipe: SR)
    fun unpackDeltaRecipe(entities: Entities, recipe: DR)
}
