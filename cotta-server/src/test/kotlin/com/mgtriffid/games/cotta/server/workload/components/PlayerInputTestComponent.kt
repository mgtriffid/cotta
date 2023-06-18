package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.entities.InputComponent

interface PlayerInputTestComponent : InputComponent<PlayerInputTestComponent> {
    companion object {
        fun createBlank(): PlayerInputTestComponent = PlayerInputTestComponentImpl(
            aim = 0,
            shoot = false
        )

        fun create(aim: Int, shoot: Boolean): PlayerInputTestComponent = PlayerInputTestComponentImpl(
            aim = aim,
            shoot = shoot
        )
    }
    var aim: Int
    var shoot: Boolean
}

private data class PlayerInputTestComponentImpl(
    override var aim: Int,
    override var shoot: Boolean
): PlayerInputTestComponent
