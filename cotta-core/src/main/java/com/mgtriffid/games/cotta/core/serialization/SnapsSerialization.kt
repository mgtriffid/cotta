package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsTraceRecipe
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

interface SnapsSerialization<SR : StateRecipe, DR : DeltaRecipe> {
    fun serializeDeltaRecipe(recipe: DR): ByteArray
    fun deserializeDeltaRecipe(bytes: ByteArray): DR
    fun serializeStateRecipe(recipe: SR): ByteArray
    fun deserializeStateRecipe(bytes: ByteArray): SR
    fun serializeEntityId(entityId: EntityId): ByteArray
    fun deserializeEntityId(bytes: ByteArray): EntityId
    fun serializeEntityCreationTraces(traces: List<Pair<MapsTraceRecipe, EntityId>>): ByteArray
    fun deserializeEntityCreationTraces(bytes: ByteArray): List<Pair<MapsTraceRecipe, EntityId>>
    fun serializePlayersSawTicks(playersSawTicks: Map<PlayerId, Long>): ByteArray
    fun deserializePlayersSawTicks(bytes: ByteArray): Map<PlayerId, Long>
}
