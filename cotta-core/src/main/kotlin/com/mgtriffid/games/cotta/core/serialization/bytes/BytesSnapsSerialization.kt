package com.mgtriffid.games.cotta.core.serialization.bytes

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.MapSerializer
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesChangedEntityRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesComponentDeltaRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesComponentRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesCottaTraceDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesCottaTraceElementDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesCreateEntityTraceDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesCreateEntityTracesDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesCreatedEntitiesWithTracesRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesDeltaRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesEffectRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesEntityRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesMetaEntitiesDeltaRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesStateRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesChangedEntityRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesComponentDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesEntityRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesPlayersDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesStateRecipe
import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto
import com.mgtriffid.games.cotta.core.serialization.dto.EntityOwnedByDto
import com.mgtriffid.games.cotta.core.serialization.dto.PlayerIdDto
import com.mgtriffid.games.cotta.core.serialization.dto.PlayersSawTicksDto
import com.mgtriffid.games.cotta.core.serialization.dto.TraceElementDtoKind
import com.mgtriffid.games.cotta.core.serialization.toDto
import com.mgtriffid.games.cotta.core.serialization.toEntityId
import com.mgtriffid.games.cotta.core.serialization.toOwnedBy

class BytesSnapsSerialization : SnapsSerialization<
    BytesStateRecipe,
    BytesDeltaRecipe,
    BytesPlayersDeltaRecipe
    > {
    private val kryo = Kryo()

    init {
        kryo.register(ByteArray::class.java)
        kryo.register(Entity.OwnedBy.Player::class.java)
        kryo.register(PlayerId::class.java)
        kryo.register(Entity.OwnedBy.System::class.java)
        kryo.register(BytesComponentDeltaRecipeDto::class.java)
        kryo.register(BytesComponentRecipeDto::class.java)
        kryo.register(BytesChangedEntityRecipeDto::class.java)
        kryo.register(BytesDeltaRecipeDto::class.java)
        kryo.register(BytesEntityRecipeDto::class.java)
        kryo.register(BytesStateRecipeDto::class.java)
        kryo.register(EntityOwnedByDto::class.java)
        kryo.register(ArrayList::class.java, CollectionSerializer<ArrayList<Any?>>())
        kryo.register(HashMap::class.java, MapSerializer<HashMap<String, Any?>>())
        kryo.register(LinkedHashMap::class.java, MapSerializer<LinkedHashMap<String, Any?>>())
        kryo.register(EntityIdDto::class.java)
        kryo.register(EntityIdDto.Kind::class.java)
        kryo.register(PlayerIdDto::class.java)
        kryo.register(BytesCreateEntityTraceDto::class.java)
        kryo.register(BytesCreateEntityTracesDto::class.java)
        kryo.register(BytesCreatedEntitiesWithTracesRecipeDto::class.java)
        kryo.register(PlayersSawTicksDto::class.java)
        kryo.register(BytesCottaTraceDto::class.java)
        kryo.register(BytesCottaTraceElementDto::class.java)
        kryo.register(TraceElementDtoKind::class.java)
        kryo.register(BytesEffectRecipeDto::class.java)
        kryo.register(BytesMetaEntitiesDeltaRecipeDto::class.java)
    }

    override fun serializeDeltaRecipe(recipe: BytesDeltaRecipe): ByteArray {
        val output = Output(4096, 1024 * 1024)
        kryo.writeObject(output, recipe.toDto())
        return output.toBytes()
    }

    override fun deserializeDeltaRecipe(bytes: ByteArray): BytesDeltaRecipe {
        return kryo.readObject(Input(bytes), BytesDeltaRecipeDto::class.java).toRecipe()
    }

    override fun serializeStateRecipe(recipe: BytesStateRecipe): ByteArray {
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, recipe.toDto())
        return output.toBytes()
    }

    override fun serializePlayersDeltaRecipe(recipe: BytesPlayersDeltaRecipe): ByteArray {
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, recipe.toDto())
        return output.toBytes()
    }

    override fun deserializePlayersDeltaRecipe(bytes: ByteArray): BytesPlayersDeltaRecipe {
        return kryo.readObject(Input(bytes), BytesMetaEntitiesDeltaRecipeDto::class.java).toRecipe()
    }

    override fun deserializeStateRecipe(bytes: ByteArray): BytesStateRecipe {
        return kryo.readObject(Input(bytes), BytesStateRecipeDto::class.java).toRecipe()
    }

    override fun serializeEntityId(entityId: EntityId): ByteArray {
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, entityId.toDto())
        return output.toBytes()
    }

    override fun deserializeEntityId(bytes: ByteArray): EntityId {
        return kryo.readObject(Input(bytes), EntityIdDto::class.java).toEntityId()
    }

    override fun serializePlayerId(playerId: PlayerId): ByteArray {
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, PlayerIdDto()
            .also {
                it.playerId = playerId.id
            })
        return output.toBytes()
    }

    override fun deserializePlayerId(bytes: ByteArray): PlayerId {
        val dto = kryo.readObject(Input(bytes), PlayerIdDto::class.java)
        return PlayerId(dto.playerId)
    }

    override fun serializePlayersSawTicks(playersSawTicks: Map<PlayerId, Long>): ByteArray {
        val output = Output(64, 1024 * 1024)
        val dto = PlayersSawTicksDto()
        dto.playersSawTicks = playersSawTicks.mapKeys { (playerId, _) ->
            playerId.id
        }
        kryo.writeObject(output, dto)
        return output.toBytes()
    }

    override fun deserializePlayersSawTicks(bytes: ByteArray): Map<PlayerId, Long> {
        return kryo.readObject(
            Input(bytes),
            PlayersSawTicksDto::class.java
        ).playersSawTicks.mapKeys { (playerId, _) ->
            PlayerId(playerId)
        }
    }
}

fun BytesComponentDeltaRecipe.toDto(): BytesComponentDeltaRecipeDto {
    val ret = BytesComponentDeltaRecipeDto()
    ret.data = data
    return ret
}

fun BytesComponentRecipe.toDto(): BytesComponentRecipeDto {
    val ret = BytesComponentRecipeDto()
    ret.data = data
    return ret
}

fun BytesChangedEntityRecipe.toDto(): BytesChangedEntityRecipeDto {
    val ret = BytesChangedEntityRecipeDto()
    ret.entityId = entityId.toDto()
    ret.addedComponents = if (addedComponents.isEmpty()) ArrayList() else ArrayList(addedComponents.map { it.toDto() })
    ret.changedComponents = if (changedComponents.isEmpty()) ArrayList() else ArrayList(changedComponents.map { it.toDto() })
    ret.removedComponents = if (removedComponents.isEmpty()) ArrayList() else ArrayList(removedComponents.map { it.key })
    return ret
}

fun BytesDeltaRecipe.toDto(): BytesDeltaRecipeDto {
    val ret = BytesDeltaRecipeDto()
    ret.addedEntities = if (addedEntities.isEmpty()) ArrayList() else ArrayList(addedEntities.map { it.toDto() })
    ret.changedEntities = if (changedEntities.isEmpty()) ArrayList() else ArrayList(changedEntities.map { it.toDto() })
    ret.removedEntitiesIds = if (removedEntitiesIds.isEmpty()) ArrayList() else ArrayList(removedEntitiesIds.map { it.toDto() })
    ret.idSequence = idSequence
    return ret
}

private fun BytesPlayersDeltaRecipe.toDto(): BytesMetaEntitiesDeltaRecipeDto {
    val ret = BytesMetaEntitiesDeltaRecipeDto()
    ret.added = addedPlayers.map { playerId ->
        val dto =
            PlayerIdDto()
        dto.playerId = playerId.id
        dto
    }
    return ret
}

private fun BytesMetaEntitiesDeltaRecipeDto.toRecipe(): BytesPlayersDeltaRecipe {
    return BytesPlayersDeltaRecipe(
        added.map { PlayerId(it.playerId) },
    )
}

private fun BytesEntityRecipe.toDto(): BytesEntityRecipeDto {
    val ret = BytesEntityRecipeDto()
    ret.entityId = entityId.toDto()
    ret.ownedBy = ownedBy.toDto()
    ret.components = ArrayList(components.map { it.toDto() })
    return ret
}

fun BytesStateRecipe.toDto(): BytesStateRecipeDto {
    val ret = BytesStateRecipeDto()
    ret.entities = ArrayList(entities.map { it.toDto() })
    return ret
}

fun BytesComponentDeltaRecipeDto.toRecipe(): BytesComponentDeltaRecipe {
    return BytesComponentDeltaRecipe(data)
}

fun BytesComponentRecipeDto.toRecipe(): BytesComponentRecipe {
    return BytesComponentRecipe(data)
}

fun BytesChangedEntityRecipeDto.toRecipe(): BytesChangedEntityRecipe {
    return BytesChangedEntityRecipe(
        entityId.toEntityId(),
        addedComponents.map { it.toRecipe() },
        changedComponents.map { it.toRecipe() },
        removedComponents.map(::ShortComponentKey)
    )
}

fun BytesDeltaRecipeDto.toRecipe(): BytesDeltaRecipe {
    return BytesDeltaRecipe(
        addedEntities.map { it.toRecipe() },
        changedEntities.map { it.toRecipe() },
        removedEntitiesIds.map { it.toEntityId() }.toSet(),
        idSequence
    )
}

fun BytesEntityRecipeDto.toRecipe(): BytesEntityRecipe {
    return BytesEntityRecipe(
        entityId.toEntityId(),
        ownedBy.toOwnedBy(),
        components.map { it.toRecipe() },
    )
}

fun BytesStateRecipeDto.toRecipe() = BytesStateRecipe(
    entities = entities.map { it.toRecipe() },
)
