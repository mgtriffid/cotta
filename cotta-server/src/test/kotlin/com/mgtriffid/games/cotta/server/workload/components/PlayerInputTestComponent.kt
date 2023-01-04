package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.entities.InputComponent

interface PlayerInputTestComponent : InputComponent {
    companion object {
        fun create(): PlayerInputTestComponent = PlayerInputTestComponentImpl(
            aim = 0,
            shoot = false
        )
    }
    var aim: Int
    var shoot: Boolean
}

private data class PlayerInputTestComponentImpl(
    override var aim: Int,
    override var shoot: Boolean
): PlayerInputTestComponent
