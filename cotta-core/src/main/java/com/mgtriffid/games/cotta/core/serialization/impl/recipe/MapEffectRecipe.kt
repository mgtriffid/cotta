package com.mgtriffid.games.cotta.core.serialization.impl.recipe

import com.mgtriffid.games.cotta.core.registry.StringEffectKey
import com.mgtriffid.games.cotta.core.serialization.EffectRecipe

class MapEffectRecipe(
    val effectKey: StringEffectKey, val data: Map<String, Any>
) : EffectRecipe