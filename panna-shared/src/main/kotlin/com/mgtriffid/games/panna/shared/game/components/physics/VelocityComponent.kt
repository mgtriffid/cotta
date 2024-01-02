package com.mgtriffid.games.panna.shared.game.components.physics

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface VelocityComponent : MutableComponent<VelocityComponent> {
    @ComponentData
    var velX: Int
    @ComponentData
    var velY: Int

    companion object {
        fun create(velX: Int, velY: Int): VelocityComponent {
            return VelocityComponentImpl(velX, velY)
        }
    }
}

private data class VelocityComponentImpl(
    override var velX: Int,
    override var velY: Int
) : VelocityComponent {
    override fun copy(): VelocityComponent {
        return this.copy(velX = velX, velY = velY)
    }
}
