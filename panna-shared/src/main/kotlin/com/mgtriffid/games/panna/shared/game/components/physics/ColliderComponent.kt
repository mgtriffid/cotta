package com.mgtriffid.games.panna.shared.game.components.physics

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

interface ColliderComponent : Component<ColliderComponent> {
    @ComponentData val width: Int
    @ComponentData val height: Int

    companion object {
        fun create(width: Int, height: Int): ColliderComponent {
            return ColliderComponentImpl(width, height)
        }
    }
}

private data class ColliderComponentImpl(
    override val width: Int,
    override val height: Int
) : ColliderComponent {
    override fun copy(): ColliderComponent = this
}