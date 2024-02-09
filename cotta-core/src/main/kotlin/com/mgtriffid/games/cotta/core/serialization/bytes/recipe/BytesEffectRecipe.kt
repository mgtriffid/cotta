package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.registry.ShortEffectKey
import com.mgtriffid.games.cotta.core.serialization.EffectRecipe

class BytesEffectRecipe(
    val effectKey: ShortEffectKey, val data: ByteArray
) : EffectRecipe
