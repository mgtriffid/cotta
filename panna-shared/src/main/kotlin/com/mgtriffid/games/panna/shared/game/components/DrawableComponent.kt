package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

interface DrawableComponent : Component<DrawableComponent> {
    @ComponentData val drawStrategy: Int

    companion object {
        fun create(drawStrategy: Int): DrawableComponent {
            return DrawableComponentImpl(drawStrategy)
        }
    }

    override fun copy(): DrawableComponent = this
}

data class DrawableComponentImpl(override val drawStrategy: Int): DrawableComponent
