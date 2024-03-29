package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.serialization.StateRecipe

class BytesStateRecipe(
    override val entities: List<BytesEntityRecipe>,
) : StateRecipe
