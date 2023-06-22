package com.mgtriffid.games.cotta.core.serialization

interface InputSerialization<IR: InputRecipe> {
    fun serializeInputRecipe(recipe: IR): ByteArray
    fun deserializeInputRecipe(bytes: ByteArray): IR
}
