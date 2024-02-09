package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.entities.InputComponent

interface PlayerInputTestComponent : InputComponent<PlayerInputTestComponent> {
    companion object {
        fun createBlank(): PlayerInputTestComponent = PlayerInputTestComponentImpl(
            aim = 0,
            shoot = false
        )
    }
    val aim: Int
    val shoot: Boolean
}
