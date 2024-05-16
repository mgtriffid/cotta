package com.mgtriffid.games.panna.shared.game.components.physics

import com.mgtriffid.games.cotta.core.annotations.Component
import com.mgtriffid.games.cotta.core.annotations.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@Component
interface VelocityComponent : MutableComponent {
    @ComponentData
    var velX: Float
    @ComponentData
    var velY: Float

}
