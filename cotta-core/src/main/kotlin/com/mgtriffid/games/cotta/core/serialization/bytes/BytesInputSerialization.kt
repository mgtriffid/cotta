package com.mgtriffid.games.cotta.core.serialization.bytes

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.MapSerializer
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.entities.id.StaticEntityId
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesEntityInputRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesInputComponentRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesInputRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.dto.BytesMetaEntitiesDeltaRecipeDto
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesEntityInputRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesInputComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesInputRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesMetaEntitiesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.dto.EntityIdDto
import com.mgtriffid.games.cotta.core.serialization.dto.MetaEntityPlayerIdDto
import com.mgtriffid.games.cotta.core.serialization.toEntityId

class BytesInputSerialization : InputSerialization<BytesInputRecipe> {
    // TODO this is a potential source of confusion for someone who digs into the code.
    //      Like, why are there so many instances of Kryo, and why are they all being registered with different classes?
    private val kryo = Kryo()

    init {
        kryo.register(ByteArray::class.java)
        kryo.register(BytesInputComponentRecipeDto::class.java)
        kryo.register(BytesEntityInputRecipeDto::class.java)
        kryo.register(EntityIdDto::class.java)
        kryo.register(EntityIdDto.Kind::class.java)
        kryo.register(BytesInputRecipeDto::class.java)
        kryo.register(HashMap::class.java, MapSerializer<HashMap<String, Any?>>())
        kryo.register(ArrayList::class.java, CollectionSerializer<ArrayList<Any?>>())
    }

    override fun serializeInputRecipe(recipe: BytesInputRecipe): ByteArray {
        val output = Output(4096, 1024 * 1024)
        kryo.writeObject(output, recipe.toDto())
        return output.toBytes()
    }

    override fun deserializeInputRecipe(bytes: ByteArray): BytesInputRecipe {
        return kryo.readObject(Input(bytes), BytesInputRecipeDto::class.java).toRecipe()
    }
}

// <editor-fold desc="Converters">
fun BytesInputRecipe.toDto(): BytesInputRecipeDto {
    val ret = BytesInputRecipeDto()
    ret.entityInputs = ArrayList(entityInputs.map { it.toDto() })
    return ret
}

private fun BytesEntityInputRecipe.toDto(): BytesEntityInputRecipeDto {
    val ret = BytesEntityInputRecipeDto()
    ret.entityId = entityId.toDto()
    ret.components = ArrayList(inputComponents.map { it.toDto() })
    return ret
}

private fun BytesInputComponentRecipe.toDto(): BytesInputComponentRecipeDto {
    val ret = BytesInputComponentRecipeDto()
    ret.data = data
    return ret
}

private fun BytesInputRecipeDto.toRecipe(): BytesInputRecipe {
    return BytesInputRecipe(entityInputs.map { it.toRecipe() })
}

private fun BytesEntityInputRecipeDto.toRecipe(): BytesEntityInputRecipe {
    return BytesEntityInputRecipe(entityId.toEntityId(), components.map { it.toRecipe() })
}

private fun BytesInputComponentRecipeDto.toRecipe(): BytesInputComponentRecipe {
    return BytesInputComponentRecipe(data)
}

fun EntityId.toDto(): EntityIdDto {
    return when (this) {
        is AuthoritativeEntityId -> EntityIdDto().also {
            it.id = id
            it.kind = EntityIdDto.Kind.AUTHORITATIVE
        }
        is PredictedEntityId -> EntityIdDto().also {
            it.id = id
            it.kind = EntityIdDto.Kind.PREDICTED
            it.playerId = playerId.id
        }
        is StaticEntityId -> EntityIdDto().also {
            it.id = id
            it.kind = EntityIdDto.Kind.STATIC
        }
    }
}
// </editor-fold>
