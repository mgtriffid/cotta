package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.client.annotation.Interpolated
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface PositionComponent : MutableComponent<PositionComponent> {
    companion object {
        const val ORIENTATION_LEFT = 0
        const val ORIENTATION_RIGHT = 1

        fun create(xPos: Float, yPos: Float, orientation: Int): PositionComponent {
            return PositionComponentImpl(xPos, yPos, orientation)
        }
    }

    @Interpolated @ComponentData var xPos: Float
    @Interpolated @ComponentData var yPos: Float
    @ComponentData var orientation: Int
}
private data class PositionComponentImpl(
    override var xPos: Float,
    override var yPos: Float,
    override var orientation: Int
) : PositionComponent {
    override fun copy(): PositionComponent = this.copy(xPos = xPos, yPos = yPos, orientation = orientation)
}
