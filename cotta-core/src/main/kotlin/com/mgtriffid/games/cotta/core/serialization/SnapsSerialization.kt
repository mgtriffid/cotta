package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface SnapsSerialization<
    SR : StateRecipe,
    DR : DeltaRecipe,
    PDR: PlayersDeltaRecipe
    > {
    fun serializeDeltaRecipe(recipe: DR): ByteArray
    fun deserializeDeltaRecipe(bytes: ByteArray): DR
    fun serializeStateRecipe(recipe: SR): ByteArray
    fun deserializeStateRecipe(bytes: ByteArray): SR
    fun serializeEntityId(entityId: EntityId): ByteArray
    fun deserializeEntityId(bytes: ByteArray): EntityId
    fun serializePlayerId(playerId: PlayerId): ByteArray
    fun deserializePlayerId(bytes: ByteArray): PlayerId
    fun serializePlayersSawTicks(playersSawTicks: Map<PlayerId, Long>): ByteArray
    fun deserializePlayersSawTicks(bytes: ByteArray): Map<PlayerId, Long>
    fun serializePlayersDeltaRecipe(recipe: PDR): ByteArray
    fun deserializePlayersDeltaRecipe(bytes: ByteArray): PDR
}
