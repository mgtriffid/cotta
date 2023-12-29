package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.client.annotation.Interpolated
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface PositionComponent : MutableComponent<PositionComponent> {
    companion object {
        const val ORIENTATION_LEFT = 0
        const val ORIENTATION_RIGHT = 1

        fun create(xPos: Int, yPos: Int, orientation: Int): PositionComponent {
            return PositionComponentImpl(xPos, yPos, orientation)
        }
    }

    @Interpolated @ComponentData var xPos: Int
    @Interpolated @ComponentData var yPos: Int
    @ComponentData var orientation: Int
}
private data class PositionComponentImpl(
    override var xPos: Int,
    override var yPos: Int,
    override var orientation: Int
) : PositionComponent {
    override fun copy(): PositionComponent = this.copy(xPos = xPos, yPos = yPos, orientation = orientation)
}
