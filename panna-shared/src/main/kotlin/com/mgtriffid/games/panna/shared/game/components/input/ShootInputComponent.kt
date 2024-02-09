package com.mgtriffid.games.panna.shared.game.components.input

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.InputComponent

@com.mgtriffid.games.cotta.Component
interface ShootInputComponent : InputComponent<ShootInputComponent> {

    @ComponentData
    val isShooting: Boolean

    companion object {
        fun createBlank(): ShootInputComponent = ShootInputComponentImpl(
            isShooting = false
        )
    }
}
