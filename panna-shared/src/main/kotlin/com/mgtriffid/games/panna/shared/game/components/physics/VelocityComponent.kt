package com.mgtriffid.games.panna.shared.game.components.physics

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface VelocityComponent : MutableComponent<VelocityComponent> {
    @ComponentData
    var velX: Float
    @ComponentData
    var velY: Float

    companion object {
        fun create(velX: Float, velY: Float): VelocityComponent {
            return VelocityComponentImpl(velX, velY)
        }
    }
}

private data class VelocityComponentImpl(
    override var velX: Float,
    override var velY: Float
) : VelocityComponent {
    override fun copy(): VelocityComponent {
        return this.copy(velX = velX, velY = velY)
    }
}
