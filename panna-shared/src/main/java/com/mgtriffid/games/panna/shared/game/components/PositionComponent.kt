package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.entities.Component

data class PositionComponent(
    var x: Float,
    var y: Float,
    var orientation: Orientation
) : Component {
    enum class Orientation {
        LEFT, RIGHT
    }
}
