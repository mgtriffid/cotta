package com.mgtriffid.games.panna.shared.game.components.input

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.InputComponent

interface ShootInputComponent : InputComponent<ShootInputComponent> {

    @ComponentData
    val isShooting: Boolean

    companion object {
        fun createBlank(): ShootInputComponent = ShootInputComponentImpl(
            isShooting = false
        )

        fun create(isShooting: Boolean): ShootInputComponent {
            return ShootInputComponentImpl(isShooting)
        }
    }
}

private data class ShootInputComponentImpl(
    override val isShooting: Boolean
) : ShootInputComponent
