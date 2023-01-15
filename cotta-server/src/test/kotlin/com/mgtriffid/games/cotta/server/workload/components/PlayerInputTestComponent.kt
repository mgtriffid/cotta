package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.entities.InputComponent

interface PlayerInputTestComponent : InputComponent<PlayerInputTestComponent> {
    companion object {
        fun create(): PlayerInputTestComponent = PlayerInputTestComponentImpl(
            aim = 0,
            shoot = false
        )
    }
    var aim: Int
    var shoot: Boolean
    override fun copy(): PlayerInputTestComponent = PlayerInputTestComponentImpl(aim, shoot)
}

private data class PlayerInputTestComponentImpl(
    override var aim: Int,
    override var shoot: Boolean
): PlayerInputTestComponent
