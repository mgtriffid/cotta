package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.entities.id.EntityId

sealed interface BytesTraceElementRecipe {
    class BytesInputTraceElementRecipe(val entityId: EntityId) : BytesTraceElementRecipe
    class BytesEffectTraceElementRecipe(val effectRecipe: BytesEffectRecipe) : BytesTraceElementRecipe
    class BytesEntityProcessingTraceElementRecipe : BytesTraceElementRecipe // TODO
}
