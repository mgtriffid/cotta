package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent

interface InputSnapper<IR: InputRecipe> {
    fun snapInput(input: Map<EntityId, Collection<InputComponent<*>>>): IR
    fun unpackInputRecipe(recipe: IR): Map<EntityId, Collection<InputComponent<*>>>
}
