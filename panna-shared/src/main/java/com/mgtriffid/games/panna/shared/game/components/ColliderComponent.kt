package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

interface ColliderComponent: Component<ColliderComponent> {
    @ComponentData val width: Float
    @ComponentData val height: Float

    companion object {
        fun create(width: Float, height: Float): ColliderComponent {
            return ColliderComponentImpl(width, height)
        }
    }

    override fun copy(): ColliderComponent { return this }
}

data class ColliderComponentImpl(override val width: Float, override val height: Float) : ColliderComponent
