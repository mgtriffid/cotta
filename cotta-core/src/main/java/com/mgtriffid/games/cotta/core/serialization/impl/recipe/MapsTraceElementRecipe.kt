package com.mgtriffid.games.cotta.core.serialization.impl.recipe

import com.mgtriffid.games.cotta.core.entities.EntityId

sealed interface MapsTraceElementRecipe {
    class MapsInputTraceElementRecipe(val entityId: EntityId) : MapsTraceElementRecipe
    class MapsEffectTraceElementRecipe(val effectRecipe: MapEffectRecipe) : MapsTraceElementRecipe
    class MapsEntityProcessingTraceElementRecipe : MapsTraceElementRecipe // TODO
}
