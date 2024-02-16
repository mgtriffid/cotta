package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.serialization.TraceRecipe

class BytesTraceRecipe(
    override val elements: List<BytesTraceElementRecipe>
) : TraceRecipe
