package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

interface VelocityComponent : Component<VelocityComponent> {
    @ComponentData val velX: Int
    @ComponentData val velY: Int

    companion object {
        fun create(velX: Int, velY: Int): VelocityComponent {
            return VelocityComponentImpl(velX, velY)
        }
    }
}

private data class VelocityComponentImpl(
    override val velX: Int,
    override val velY: Int
) : VelocityComponent {
    override fun copy(): VelocityComponent {
        return this.copy(velX = velX, velY = velY)
    }
}
