package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds
import com.mgtriffid.games.panna.screens.game.graphics.textures.LeftRightRegions
import com.mgtriffid.games.panna.screens.game.graphics.textures.PannaTextures

class ActorFactory {
    private lateinit var regions: Regions // TODO name this better, it's not only regions

    fun initialize(textures: PannaTextures) {
        regions = Regions(
            dude = Regions.DudeRegions(
                body = textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_BLUE_BODY],
                feetInAir = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_IN_AIR]),
                feetOnGround = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_ON_GROUND]),
                feetRunning = listOf(
                    100L to LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_RUNNING_FRAME_0]),
                    100L to LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_DUDE_FEET_RUNNING_FRAME_1]),
                ),
                eyes = Regions.DudeRegions.EyesRegions(
                    lookingUp = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_UP]),
                    lookingStraight = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_STRAIGHT]),
                    lookingDown = LeftRightRegions(textures[PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_DOWN])
                ),
                healthBar = Regions.DudeRegions.HealthBarRegions(
                    healthBarBackground = textures[PannaTextureIds.Characters.TEXTURE_ID_HEALTH_BAR_BACKGROUND],
                    healthBarForeground = textures[PannaTextureIds.Characters.TEXTURE_ID_HEALTH_BAR_FOREGROUND]
                )
            ),
            bullet = textures[PannaTextureIds.TEXTURE_ID_BULLET],
            terrain = textures[PannaTextureIds.Terrain.TEXTURE_ID_BROWN_BLOCK],
            bulletHitsGround = listOf(
                textures[PannaTextureIds.Effects.TEXTURE_ID_BULLET_HITS_TERRAIN_FRAME_0],
                textures[PannaTextureIds.Effects.TEXTURE_ID_BULLET_HITS_TERRAIN_FRAME_1],
                textures[PannaTextureIds.Effects.TEXTURE_ID_BULLET_HITS_TERRAIN_FRAME_2],
                textures[PannaTextureIds.Effects.TEXTURE_ID_BULLET_HITS_TERRAIN_FRAME_3],
            )
        )
    }

    fun createDude(): DudeActor {
        return DudeActor(
            regions.dude
        )
    }

    fun createSolidTerrainTile(): SolidTerrainTileActor {
        return SolidTerrainTileActor(regions.terrain)
    }

    fun createBullet(): BulletActor {
        return BulletActor(regions.bullet)
    }

    fun createBulletHitsGroundVisualEffect(): BulletHitsGroundVisualEffectActor {
        return BulletHitsGroundVisualEffectActor(regions.bulletHitsGround)
    }

    class Regions(
        val dude: DudeRegions,
        val bullet: TextureRegion,
        val terrain: TextureRegion,
        val bulletHitsGround: List<TextureRegion>,
    ) {
        class DudeRegions(
            val body: TextureRegion,
            val feetInAir: LeftRightRegions,
            val feetOnGround: LeftRightRegions,
            val feetRunning: List<Pair<Long, LeftRightRegions>>,
            val eyes: EyesRegions,
            val healthBar: HealthBarRegions
        ) {
            class EyesRegions(
                val lookingUp: LeftRightRegions,
                val lookingStraight: LeftRightRegions,
                val lookingDown: LeftRightRegions,
            )
            class HealthBarRegions(
                val healthBarBackground: TextureRegion,
                val healthBarForeground: TextureRegion
            )
        }
    }
}
