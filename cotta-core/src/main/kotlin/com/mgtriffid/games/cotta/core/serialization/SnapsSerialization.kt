package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.CreatedEntitiesWithTracesRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsTraceRecipe

interface SnapsSerialization<SR : StateRecipe, DR : DeltaRecipe> {
    fun serializeDeltaRecipe(recipe: DR): ByteArray
    fun deserializeDeltaRecipe(bytes: ByteArray): DR
    fun serializeStateRecipe(recipe: SR): ByteArray
    fun deserializeStateRecipe(bytes: ByteArray): SR
    fun serializeEntityId(entityId: EntityId): ByteArray
    fun deserializeEntityId(bytes: ByteArray): EntityId
    fun serializeMetaEntityId(entityId: EntityId, playerId: PlayerId): ByteArray
    fun deserializeMetaEntityId(bytes: ByteArray): Pair<EntityId, PlayerId>
    fun serializeEntityCreationTraces(traces: List<Pair<MapsTraceRecipe, EntityId>>): ByteArray
    fun serializeEntityCreationTracesV2(createdEntities: CreatedEntitiesWithTracesRecipe): ByteArray
    fun deserializeEntityCreationTraces(bytes: ByteArray): List<Pair<MapsTraceRecipe, EntityId>>
    fun deserializeEntityCreationTracesV2(bytes: ByteArray): CreatedEntitiesWithTracesRecipe
    fun serializePlayersSawTicks(playersSawTicks: Map<PlayerId, Long>): ByteArray
    fun deserializePlayersSawTicks(bytes: ByteArray): Map<PlayerId, Long>
}
