package com.mgtriffid.games.cotta.core.serialization.bytes.recipe

import com.mgtriffid.games.cotta.core.serialization.InputRecipe

class BytesInputRecipe(
    override val entityInputs: List<BytesEntityInputRecipe>
) : InputRecipe
