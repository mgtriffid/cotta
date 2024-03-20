package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@com.mgtriffid.games.cotta.Component
interface CharacterInputComponent2 :
    MutableComponent<CharacterInputComponent2> {

    @ComponentData
    var direction: Byte
    @ComponentData
    var jump: Boolean
    @ComponentData
    var lookAt: Float
    @ComponentData
    var switchWeapon: Byte
}

const val WALKING_DIRECTION_NONE: Byte = 0x00
const val WALKING_DIRECTION_LEFT: Byte = 0x01
const val WALKING_DIRECTION_RIGHT: Byte = 0x02
