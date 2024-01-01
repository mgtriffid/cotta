package com.mgtriffid.games.panna.graphics.textures

import com.badlogic.gdx.graphics.Texture
import com.mgtriffid.games.panna.shared.game.components.PannaTextureIds

class PannaTextures {
    private val textures = HashMap<Int, Texture>()

    fun init() {
        textures[PannaTextureIds.TEXTURE_ID_FOO_ENTITY] = Texture("characters-free-sprites/2 GraveRobber/Graverobber.png")
        textures[PannaTextureIds.TEXTURE_ID_PLAYER_ENTITY] = Texture("characters-free-sprites/3 SteamMan/SteamMan.png")
        textures[PannaTextureIds.TEXTURE_ID_BULLET] = Texture("bullet.png")
        textures[PannaTextureIds.TEXTURE_ID_TERRAIN] = Texture("terrain.png")
        textures[PannaTextureIds.Terrain.TEXTURE_ID_BROWN_BLOCK] = Texture("panna/terrain/brown-block.png")
        textures[PannaTextureIds.Characters.TEXTURE_ID_TRIGGERMAN] = Texture("panna/characters/triggerman.png")
    }

    fun dispose() {
        textures.values.forEach { it.dispose() }
    }

    operator fun get(textureId: Int): Texture = textures[textureId] ?:
        throw IllegalArgumentException("Unknown texture id: $textureId")
}
