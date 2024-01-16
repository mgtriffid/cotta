package com.mgtriffid.games.panna.shared.game.components.input

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.InputComponent

interface CharacterInputComponent : InputComponent<CharacterInputComponent> {

    @ComponentData
    val direction: Byte
    @ComponentData
    val jump: Boolean
    @ComponentData
    val lookAt: Float

    companion object {
        fun createBlank(): CharacterInputComponent = CharacterInputComponentImpl(
            direction = WALKING_DIRECTION_NONE,
            lookAt = 0f,
            jump = false
        )

        fun create(direction: Byte, jump: Boolean, lookAt: Float): CharacterInputComponent {
            return CharacterInputComponentImpl(direction, jump, lookAt)
        }
    }
}

private data class CharacterInputComponentImpl(
    override val direction: Byte,
    override val jump: Boolean,
    override val lookAt: Float
) : CharacterInputComponent

const val WALKING_DIRECTION_NONE: Byte = 0x00
const val WALKING_DIRECTION_LEFT: Byte = 0x01
const val WALKING_DIRECTION_RIGHT: Byte = 0x02
