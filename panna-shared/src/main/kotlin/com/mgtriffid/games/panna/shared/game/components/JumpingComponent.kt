package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@com.mgtriffid.games.cotta.Component
interface JumpingComponent : MutableComponent<JumpingComponent> {
    @ComponentData
    var inAir: Boolean
    @ComponentData
    val jumpSpeed: Float
}
