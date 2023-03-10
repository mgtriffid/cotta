package com.mgtriffid.games.cotta.core.serialization

interface SnapsSerialization<SR : StateRecipe, DR : DeltaRecipe> {
    fun serializeDeltaRecipe(recipe: DR): ByteArray
    fun deserializeDeltaRecipe(bytes: ByteArray): DR
    fun serializeStateRecipe(recipe: SR): ByteArray
    fun deserializeStateRecipe(bytes: ByteArray): SR
}
