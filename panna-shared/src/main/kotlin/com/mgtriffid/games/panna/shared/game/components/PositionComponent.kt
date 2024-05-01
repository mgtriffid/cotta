package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.annotations.ComponentData
import com.mgtriffid.games.cotta.client.annotation.Interpolated
import com.mgtriffid.games.cotta.core.annotations.Component
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@Component
interface PositionComponent : MutableComponent<PositionComponent> {
    @Interpolated @ComponentData
    var xPos: Float
    @Interpolated @ComponentData
    var yPos: Float
}
