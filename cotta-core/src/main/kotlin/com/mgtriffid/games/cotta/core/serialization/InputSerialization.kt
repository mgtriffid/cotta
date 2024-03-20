package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.PlayerInput

interface InputSerialization<IR: InputRecipe> {
    fun serializeInputRecipe(recipe: IR): ByteArray
    fun serializeInput(input: PlayerInput): ByteArray
    fun deserializeInputRecipe(bytes: ByteArray): IR
    fun deserializeInput(bytes: ByteArray): PlayerInput
    fun serializePlayersInputs(inputs: Map<PlayerId, PlayerInput>): ByteArray
    fun deserializePlayersInputs(bytes: ByteArray): Map<PlayerId, PlayerInput>
}
