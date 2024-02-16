package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.serialization.ComponentDeltaRecipe

// TODO cut. Not needed as long as we don't actually serialize delta.
class BytesComponentDeltaRecipe(
    val data: ByteArray
) : ComponentDeltaRecipe
