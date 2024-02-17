package com.mgtriffid.games.cotta.core.serialization.bytes

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesEntityInputRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesInputComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesInputRecipe
import jakarta.inject.Inject
import jakarta.inject.Named

class BytesInputSnapper @Inject constructor(
    @Named("snapper") private val kryo: Kryo
): InputSnapper<BytesInputRecipe> {
    override fun snapInput(input: Map<EntityId, Collection<InputComponent<*>>>): BytesInputRecipe {
        return BytesInputRecipe(input.map { (entityId, inputComponents) ->
            BytesEntityInputRecipe(
                entityId,
                inputComponents.map(::packInputComponent)
            )
        })
    }

    private fun packInputComponent(inputComponent: InputComponent<*>): BytesInputComponentRecipe {
        return BytesInputComponentRecipe(
            data = kryo.run {
                val output = Output(1024)
                writeClassAndObject(output, inputComponent)
                output.toBytes()
            }
        )
    }

    private fun unpackInputComponent(inputComponent: BytesInputComponentRecipe): InputComponent<*> {
        return kryo.run {
            readClassAndObject(Input(inputComponent.data))
        } as InputComponent<*>
    }

    override fun unpackInputRecipe(recipe: BytesInputRecipe): Map<EntityId, Collection<InputComponent<*>>> {
        return recipe.entityInputs.associate { entityInput ->
            entityInput.entityId to entityInput.inputComponents.map(::unpackInputComponent)
        }
    }
}
