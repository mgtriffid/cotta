package com.mgtriffid.games.cotta.core.serialization.impl

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.MapSerializer
import com.mgtriffid.games.cotta.core.entities.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.impl.dto.EntityIdDto
import com.mgtriffid.games.cotta.core.serialization.impl.dto.EntityOwnedByDto
import com.mgtriffid.games.cotta.core.serialization.impl.dto.MapComponentDeltaRecipeDto
import com.mgtriffid.games.cotta.core.serialization.impl.dto.MapComponentRecipeDto
import com.mgtriffid.games.cotta.core.serialization.impl.dto.MapsChangedEntityRecipeDto
import com.mgtriffid.games.cotta.core.serialization.impl.dto.MapsDeltaRecipeDto
import com.mgtriffid.games.cotta.core.serialization.impl.dto.MapsEntityRecipeDto
import com.mgtriffid.games.cotta.core.serialization.impl.dto.MapsStateRecipeDto
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapComponentDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsChangedEntityRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsEntityRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsStateRecipe

class MapsSnapsSerialization : SnapsSerialization<MapsStateRecipe, MapsDeltaRecipe> {
    // this is not thread safe
    private val kryo = Kryo()
    init {
        kryo.register(MapComponentDeltaRecipeDto::class.java)
        kryo.register(MapComponentRecipeDto::class.java)
        kryo.register(MapsChangedEntityRecipeDto::class.java)
        kryo.register(MapsDeltaRecipeDto::class.java)
        kryo.register(MapsEntityRecipeDto::class.java)
        kryo.register(MapsStateRecipeDto::class.java)
        kryo.register(EntityOwnedByDto::class.java)
        kryo.register(ArrayList::class.java, CollectionSerializer<ArrayList<Any?>>())
        kryo.register(HashMap::class.java, MapSerializer<HashMap<String, Any?>>())
        kryo.register(LinkedHashMap::class.java, MapSerializer<LinkedHashMap<String, Any?>>())
        kryo.register(EntityIdDto::class.java)
    }

    override fun serializeDeltaRecipe(recipe: MapsDeltaRecipe): ByteArray {
        val output = Output(4096, 1024 * 1024)
        kryo.writeObject(output, recipe.toDto())
        return output.toBytes()
    }

    override fun deserializeDeltaRecipe(bytes: ByteArray): MapsDeltaRecipe {
        return kryo.readObject(Input(bytes), MapsDeltaRecipeDto::class.java).toRecipe()
    }

    override fun serializeStateRecipe(recipe: MapsStateRecipe): ByteArray {
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, recipe.toDto())
        return output.toBytes()
    }

    override fun deserializeStateRecipe(bytes: ByteArray): MapsStateRecipe {
        return kryo.readObject(Input(bytes), MapsStateRecipeDto::class.java).toRecipe()
    }

    override fun serializeEntityId(entityId: EntityId): ByteArray {
        val output = Output(64, 1024 * 1024)
        kryo.writeObject(output, entityId.toDto())
        return output.toBytes()
    }

    override fun deserializeEntityId(bytes: ByteArray): EntityId {
        return kryo.readObject(Input(bytes), EntityIdDto::class.java).toEntityId()
    }
}

// <editor-fold desc="Converters">
fun MapComponentDeltaRecipe.toDto(): MapComponentDeltaRecipeDto {
    val ret = MapComponentDeltaRecipeDto()
    ret.key = componentKey.name
    ret.data = HashMap(this.data)
    return ret
}

fun MapComponentRecipe.toDto(): MapComponentRecipeDto {
    val ret = MapComponentRecipeDto()
    ret.key = componentKey.name
    ret.data = HashMap(this.data)
    return ret
}

fun MapsChangedEntityRecipe.toDto(): MapsChangedEntityRecipeDto {
    val ret = MapsChangedEntityRecipeDto()
    ret.entityId = entityId.toDto()
    ret.addedComponents = ArrayList(this.addedComponents.map(MapComponentRecipe::toDto))
    ret.removedComponents = ArrayList(removedComponents.map { it.name })
    ret.changedComponents = ArrayList(this.changedComponents.map(MapComponentDeltaRecipe::toDto))
    return ret
}

fun MapsDeltaRecipe.toDto(): MapsDeltaRecipeDto {
    val ret = MapsDeltaRecipeDto()
    ret.addedEntities = ArrayList(addedEntities.map { it.toDto() })
    ret.changedEntities = ArrayList(changedEntities.map { it.toDto() })
    ret.removedEntitiesIds = ArrayList(removedEntitiesIds.map { it.toDto() })
    return ret
}

fun MapsEntityRecipe.toDto(): MapsEntityRecipeDto {
    val ret = MapsEntityRecipeDto()
    ret.entityId = entityId.toDto()
    ret.ownedBy = ownedBy.toDto()
    ret.components = ArrayList(components.map { it.toDto() })
    ret.inputComponents = ArrayList(inputComponents.map { it.name })
    return ret
}

fun MapsStateRecipe.toDto(): MapsStateRecipeDto {
    val ret = MapsStateRecipeDto()
    ret.entities = ArrayList(entities.map { it.toDto() })
    return ret
}

fun MapComponentDeltaRecipeDto.toRecipe() = MapComponentDeltaRecipe(
    componentKey = StringComponentKey(key),
    data = data
)

fun MapComponentRecipeDto.toRecipe() = MapComponentRecipe(
    componentKey = StringComponentKey(key),
    data = data
)

fun MapsChangedEntityRecipeDto.toRecipe() = MapsChangedEntityRecipe(
    entityId = entityId.toEntityId(),
    addedComponents = addedComponents.map { it.toRecipe() },
    changedComponents = changedComponents.map { it.toRecipe() },
    removedComponents = removedComponents.map(::StringComponentKey)
)

fun MapsDeltaRecipeDto.toRecipe() = MapsDeltaRecipe(
    addedEntities = addedEntities.map { it.toRecipe() },
    changedEntities = changedEntities.map { it.toRecipe() },
    removedEntitiesIds = removedEntitiesIds.map { it.toEntityId() }. toSet()
)

fun MapsEntityRecipeDto.toRecipe() = MapsEntityRecipe(
    entityId = entityId.toEntityId(),
    ownedBy = ownedBy.toOwnedBy(),
    components = components.map { it.toRecipe() },
    inputComponents = inputComponents.map(::StringComponentKey)
)

fun MapsStateRecipeDto.toRecipe() = MapsStateRecipe(
    entities = entities.map { it.toRecipe() }
)

fun EntityId.toDto(): EntityIdDto {
    return when (this) {
        is AuthoritativeEntityId -> EntityIdDto().also { it.id = id }
        else -> TODO()
    }
}

fun EntityIdDto.toEntityId(): EntityId {
    return AuthoritativeEntityId(id)
}

fun Entity.OwnedBy.toDto(): EntityOwnedByDto {
    val ret = EntityOwnedByDto();
    when (this) {
        is Entity.OwnedBy.Player -> {
            ret.ownedBySystem = false
            ret.playerId = playerId.id
        }
        is Entity.OwnedBy.System -> {
            ret.ownedBySystem = true
            ret.playerId = 0
        }
    }
    return ret
}

fun EntityOwnedByDto.toOwnedBy(): Entity.OwnedBy {
    return if (ownedBySystem) {
        Entity.OwnedBy.System
    } else {
        Entity.OwnedBy.Player(PlayerId(playerId))
    }
}
// </editor-fold>
