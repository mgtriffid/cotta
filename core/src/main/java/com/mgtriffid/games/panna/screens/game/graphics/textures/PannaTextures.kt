package com.mgtriffid.games.panna.screens.game.graphics.textures

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds
import com.mgtriffid.games.panna.screens.game.graphics.TextureId

class PannaTextures {
    private val textures = HashMap<TextureId, TextureRegion>()

    fun init() {
        textures[PannaTextureIds.TEXTURE_ID_BULLET] = TextureRegion(Texture("bullet.png"))
        textures[PannaTextureIds.Terrain.TEXTURE_ID_BROWN_BLOCK] = TextureRegion(Texture("panna/terrain/brown-block.png"))
        textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_BLUE_BODY] = TextureRegion(Texture("panna/characters/dude/body.png"), 16, 16)

        val eyesTexture = Texture("panna/characters/dude/eyes.png")
        textures[PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_UP] = TextureRegion(eyesTexture, 16, 16)
        textures[PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_STRAIGHT] = TextureRegion(eyesTexture, 16, 0, 16, 16)
        textures[PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_DOWN] = TextureRegion(eyesTexture, 32, 0, 16, 16)

        val feetTexture = Texture("panna/characters/dude/feet.png")
        textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_ON_GROUND] = TextureRegion(feetTexture, 16, 16)
        textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_RUNNING_FRAME_0] = TextureRegion(feetTexture, 16, 16)
        textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_RUNNING_FRAME_1] = TextureRegion(feetTexture, 16, 0,  16, 16)
        textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_IN_AIR] = TextureRegion(feetTexture, 32, 0, 16, 16)
        textures[PannaTextureIds.Characters.TEXTURE_ID_HEALTH_BAR_BACKGROUND] = TextureRegion(Texture("panna/characters/dude/health-bar-background.png"))
        textures[PannaTextureIds.Characters.TEXTURE_ID_HEALTH_BAR_FOREGROUND] = TextureRegion(Texture("panna/characters/dude/health-bar-foreground.png"))
    }

    fun dispose() {
        // TODO make sure no texture is disposed twice
        textures.values.forEach { it.texture.dispose() }
    }

    operator fun get(textureId: TextureId): TextureRegion = textures[textureId] ?:
        throw IllegalArgumentException("Unknown texture id: $textureId")
}
