package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsTraceRecipe
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface StateSnapper<SR: StateRecipe, DR: DeltaRecipe> {
    fun snapState(entities: Entities): SR
    fun snapDelta(prev: Entities, curr: Entities): DR
    fun unpackStateRecipe(entities: Entities, recipe: SR)
    fun unpackDeltaRecipe(entities: Entities, recipe: DR)
    fun snapTrace(trace: CottaTrace): MapsTraceRecipe
    fun unpackTrace(trace: MapsTraceRecipe): CottaTrace
}
