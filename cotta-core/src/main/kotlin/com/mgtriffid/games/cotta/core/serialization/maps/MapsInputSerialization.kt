package com.mgtriffid.games.cotta.core.serialization.maps

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CollectionSerializer
import com.esotericsoftware.kryo.serializers.MapSerializer
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.maps.dto.EntityIdDto
import com.mgtriffid.games.cotta.core.serialization.maps.dto.MapInputComponentRecipeDto
import com.mgtriffid.games.cotta.core.serialization.maps.dto.MapsEntityInputRecipeDto
import com.mgtriffid.games.cotta.core.serialization.maps.dto.MapsInputRecipeDto
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsInputComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsEntityInputRecipe
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsInputRecipe

class MapsInputSerialization: InputSerialization<MapsInputRecipe> {
    private val kryo = Kryo()
    init {
        kryo.register(MapInputComponentRecipeDto::class.java)
        kryo.register(MapsEntityInputRecipeDto::class.java)
        kryo.register(EntityIdDto::class.java)
        kryo.register(EntityIdDto.Kind::class.java)
        kryo.register(MapsInputRecipeDto::class.java)
        kryo.register(HashMap::class.java, MapSerializer<HashMap<String, Any?>>())
        kryo.register(ArrayList::class.java, CollectionSerializer<ArrayList<Any?>>())
    }

    override fun serializeInputRecipe(recipe: MapsInputRecipe): ByteArray {
        val output = Output(4096, 1024 * 1024)
        kryo.writeObject(output, recipe.toDto())
        return output.toBytes()
    }

    override fun deserializeInputRecipe(bytes: ByteArray): MapsInputRecipe {
        return kryo.readObject(Input(bytes), MapsInputRecipeDto::class.java).toRecipe()
    }
}

// <editor-fold desc="Converters">
fun MapsInputRecipe.toDto(): MapsInputRecipeDto {
    val ret = MapsInputRecipeDto()
    ret.entityInputs = ArrayList(entityInputs.map { it.toDto() })
    return ret
}

private fun MapsEntityInputRecipe.toDto(): MapsEntityInputRecipeDto {
    val ret = MapsEntityInputRecipeDto()
    ret.entityId = entityId.toDto()
    ret.components = ArrayList(inputComponents.map { it.toDto() })
    return ret
}

private fun MapsInputComponentRecipe.toDto(): MapInputComponentRecipeDto {
    val ret = MapInputComponentRecipeDto()
    ret.key = componentKey.name
    ret.data = HashMap(data)
    return ret
}

fun MapsInputRecipeDto.toRecipe(): MapsInputRecipe {
    return MapsInputRecipe(entityInputs.map { it.toRecipe() })
}

private fun MapsEntityInputRecipeDto.toRecipe(): MapsEntityInputRecipe {
    return MapsEntityInputRecipe(entityId.toEntityId(), components.map { it.toRecipe() })
}

private fun MapInputComponentRecipeDto.toRecipe(): MapsInputComponentRecipe {
    return MapsInputComponentRecipe(StringComponentKey(key), HashMap(data))
}
// </editor-fold>
