package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.serialization.ComponentDeltaRecipe

class BytesComponentDeltaRecipe(
    val componentKey: ShortComponentKey,
    val data: ByteArray
) : ComponentDeltaRecipe
