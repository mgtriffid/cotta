package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

interface DrawableComponent : Component<DrawableComponent> {
    @ComponentData val textureId: Int

    companion object {
        fun create(textureId: Int): DrawableComponent {
            return DrawableComponentImpl(textureId)
        }
    }

    override fun copy(): DrawableComponent = this
}

data class DrawableComponentImpl(override val textureId: Int): DrawableComponent

object PannaTextureIds {
    const val TEXTURE_ID_FOO_ENTITY = 1
    const val TEXTURE_ID_PLAYER_ENTITY = 2
    const val TEXTURE_ID_BULLET = 3
    const val TEXTURE_ID_TERRAIN = 4
}
