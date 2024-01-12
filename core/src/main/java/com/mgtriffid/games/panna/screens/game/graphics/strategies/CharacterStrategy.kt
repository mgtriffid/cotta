package com.mgtriffid.games.panna.screens.game.graphics.strategies

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.panna.screens.game.graphics.DrawStrategy
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds
import com.mgtriffid.games.panna.screens.game.graphics.TextureId
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent

object CharacterStrategy : DrawStrategy {
    override fun getTexture(e: Entity): TextureId {
        val jumpingComponent = e.getComponent(JumpingComponent::class)
        return if (jumpingComponent.inAir) {
            PannaTextureIds.Characters.TEXTURE_ID_DUDE_BLUE_JUMPING
        } else {
            PannaTextureIds.Characters.TEXTURE_ID_DUDE_BLUE
        }
    }
}
