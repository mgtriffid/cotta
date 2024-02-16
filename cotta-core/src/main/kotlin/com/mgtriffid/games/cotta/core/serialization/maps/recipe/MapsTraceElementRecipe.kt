package com.mgtriffid.games.cotta.core.serialization.maps.recipe

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.serialization.TraceElementRecipe

sealed interface MapsTraceElementRecipe : TraceElementRecipe {
    class MapsInputTraceElementRecipe(val entityId: EntityId) : MapsTraceElementRecipe
    class MapsEffectTraceElementRecipe(val effectRecipe: MapsEffectRecipe) : MapsTraceElementRecipe
    class MapsEntityProcessingTraceElementRecipe : MapsTraceElementRecipe // TODO
}
