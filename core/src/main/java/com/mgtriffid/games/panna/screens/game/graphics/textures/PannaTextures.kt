package com.mgtriffid.games.panna.screens.game.graphics.textures

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds
import com.mgtriffid.games.panna.screens.game.graphics.TextureId

class PannaTextures {
    private val textures = HashMap<TextureId, TextureRegion>()

    fun init() {
        textures[PannaTextureIds.TEXTURE_ID_FOO_ENTITY] = TextureRegion(Texture("characters-free-sprites/2 GraveRobber/Graverobber.png"))
        textures[PannaTextureIds.TEXTURE_ID_PLAYER_ENTITY] = TextureRegion(Texture("characters-free-sprites/3 SteamMan/SteamMan.png"))
        textures[PannaTextureIds.TEXTURE_ID_BULLET] = TextureRegion(Texture("bullet.png"))
        textures[PannaTextureIds.TEXTURE_ID_TERRAIN] = TextureRegion(Texture("terrain.png"))
        textures[PannaTextureIds.Terrain.TEXTURE_ID_BROWN_BLOCK] = TextureRegion(Texture("panna/terrain/brown-block.png"))
        textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_BLUE] = TextureRegion(Texture("panna/characters/dude_blue.png"), 16, 16)
        textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_BLUE_JUMPING] = TextureRegion(Texture("panna/characters/dude_blue_jumping.png"), 16, 16)
    }

    fun dispose() {
        // TODO make sure no texture is disposed twice
        textures.values.forEach { it.texture.dispose() }
    }

    operator fun get(textureId: TextureId): TextureRegion = textures[textureId] ?:
        throw IllegalArgumentException("Unknown texture id: $textureId")
}
