package com.mgtriffid.games.panna.graphics.textures

import com.badlogic.gdx.graphics.Texture
import com.mgtriffid.games.panna.shared.game.components.PannaTextureIds

class PannaTextures {
    private val textures = HashMap<Int, Texture>()

    fun init() {
        textures[PannaTextureIds.TEXTURE_ID_FOO_ENTITY] = Texture("characters-free-sprites/2 GraveRobber/Graverobber.png")
    }

    operator fun get(textureId: Int): Texture = textures[textureId] ?:
        throw IllegalArgumentException("Unknown texture id: $textureId")
}
