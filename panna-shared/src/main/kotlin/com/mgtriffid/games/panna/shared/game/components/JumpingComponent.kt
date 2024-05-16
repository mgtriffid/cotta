package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.annotations.Component
import com.mgtriffid.games.cotta.core.annotations.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@Component
interface JumpingComponent : MutableComponent {
    @ComponentData
    var inAir: Boolean
    @ComponentData
    val jumpSpeed: Float
}
