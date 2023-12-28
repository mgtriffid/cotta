package com.mgtriffid.games.panna.shared.game.components.input

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.InputComponent

interface WalkingInputComponent : InputComponent<WalkingInputComponent> {

    @ComponentData
    val direction: Byte

    companion object {
        fun createBlank(): WalkingInputComponent = WalkingInputComponentImpl(
            direction = WALKING_DIRECTION_NONE
        )

        fun create(direction: Byte): WalkingInputComponent {
            return WalkingInputComponentImpl(direction)
        }
    }
}

private data class WalkingInputComponentImpl(
    override val direction: Byte
) : WalkingInputComponent

const val WALKING_DIRECTION_NONE: Byte = 0x00
const val WALKING_DIRECTION_LEFT: Byte = 0x01
const val WALKING_DIRECTION_RIGHT: Byte = 0x02
const val WALKING_DIRECTION_UP: Byte = 0x03
const val WALKING_DIRECTION_DOWN: Byte = 0x04
