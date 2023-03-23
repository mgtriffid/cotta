package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.EntityId

interface SnapsSerialization<SR : StateRecipe, DR : DeltaRecipe> {
    fun serializeDeltaRecipe(recipe: DR): ByteArray
    fun deserializeDeltaRecipe(bytes: ByteArray): DR
    fun serializeStateRecipe(recipe: SR): ByteArray
    fun deserializeStateRecipe(bytes: ByteArray): SR
    fun serializeEntityId(entityId: EntityId): ByteArray
    fun deserializeEntityId(bytes: ByteArray): EntityId
}
