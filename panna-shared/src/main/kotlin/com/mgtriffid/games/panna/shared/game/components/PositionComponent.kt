package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.client.annotation.Interpolated
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@com.mgtriffid.games.cotta.Component
interface PositionComponent : MutableComponent<PositionComponent> {
    @Interpolated @ComponentData var xPos: Float
    @Interpolated @ComponentData var yPos: Float
}
