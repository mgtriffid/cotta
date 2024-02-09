package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.serialization.ComponentRecipe

class BytesComponentRecipe(
    val short: ShortComponentKey,
    val data: ByteArray
) : ComponentRecipe
