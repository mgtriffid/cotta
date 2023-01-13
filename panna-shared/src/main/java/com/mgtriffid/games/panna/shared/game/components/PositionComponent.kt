package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.entities.MutableComponent

data class PositionComponent(
    var x: Float,
    var y: Float,
    var orientation: Orientation
) : MutableComponent<PositionComponent> {
    enum class Orientation {
        LEFT, RIGHT
    }

    override fun copy(): PositionComponent = this.copy()
}
