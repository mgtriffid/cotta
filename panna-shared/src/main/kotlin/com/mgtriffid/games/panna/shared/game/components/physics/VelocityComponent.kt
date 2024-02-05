package com.mgtriffid.games.panna.shared.game.components.physics

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@com.mgtriffid.games.cotta.Component
interface VelocityComponent : MutableComponent<VelocityComponent> {
    @ComponentData
    var velX: Float
    @ComponentData
    var velY: Float

}
