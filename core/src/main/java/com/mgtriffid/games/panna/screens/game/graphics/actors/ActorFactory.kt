package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds
import com.mgtriffid.games.panna.screens.game.graphics.textures.LeftRightRegions
import com.mgtriffid.games.panna.screens.game.graphics.textures.PannaTextures

class ActorFactory {
    private lateinit var regions: Regions

    fun initialize(textures: PannaTextures) {
        regions = Regions(
            dude = Regions.DudeRegions(
                body = textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_BLUE_BODY],
                feetInAir = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_IN_AIR]),
                feetOnGround = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_ON_GROUND]),
                eyes = Regions.DudeRegions.EyesRegions(
                    lookingUp = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_UP]),
                    lookingStraight = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_STRAIGHT]),
                    lookingDown = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_DOWN])
                )
            ),
            bullet = textures[PannaTextureIds.TEXTURE_ID_BULLET],
            terrain = textures[PannaTextureIds.TEXTURE_ID_TERRAIN],
        )
    }

    fun createDude(): DudeActor {
        return DudeActor(
            regions.dude
        )
    }

    class Regions(
        val dude: DudeRegions,
        val bullet: TextureRegion,
        val terrain: TextureRegion,
    ) {
        class DudeRegions(
            val body: TextureRegion,
            val feetInAir: LeftRightRegions,
            val feetOnGround: LeftRightRegions,
            val eyes: EyesRegions,
        ) {
            class EyesRegions(
                val lookingUp: LeftRightRegions,
                val lookingStraight: LeftRightRegions,
                val lookingDown: LeftRightRegions,
            )
        }
    }
}