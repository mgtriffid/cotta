package com.mgtriffid.games.panna.screens.game.graphics

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.panna.screens.game.SCALE
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds.Characters.TEXTURE_ID_DUDE_BLUE
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds.Characters.TEXTURE_ID_DUDE_BLUE_JUMPING
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_DOWN
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_STRAIGHT
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds.Characters.TEXTURE_ID_EYES_BLUE_LOOKING_UP
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds.TEXTURE_ID_BULLET
import com.mgtriffid.games.panna.screens.game.graphics.PannaTextureIds.Terrain.TEXTURE_ID_BROWN_BLOCK
import com.mgtriffid.games.panna.screens.game.graphics.actors.BulletActor
import com.mgtriffid.games.panna.screens.game.graphics.actors.DudeActor
import com.mgtriffid.games.panna.screens.game.graphics.actors.PannaActor
import com.mgtriffid.games.panna.screens.game.graphics.actors.SolidTerrainTileActor
import com.mgtriffid.games.panna.screens.game.graphics.textures.PannaTextures
import com.mgtriffid.games.panna.shared.BULLET_STRATEGY
import com.mgtriffid.games.panna.shared.CHARACTER_STRATEGY
import com.mgtriffid.games.panna.shared.SOLID_TERRAIN_TILE_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent

class GraphicsV2 {

    private lateinit var textures: PannaTextures
    private lateinit var stage: Stage
    lateinit var viewport: Viewport
    private val entityActors = HashMap<EntityId, PannaActor>()

    fun initialize() {
        textures = PannaTextures()
        textures.init()
        prepareStage()
    }

    private fun prepareStage() {
        viewport = ExtendViewport(960f, 960 * 9 / 16f)
        stage = Stage(viewport)
    }

    fun dispose() {
        stage.dispose()
    }

    fun draw(entities: List<Entity>, playerId: PlayerId, delta: Float) {
        processEntities(entities, playerId)
        stage.act(delta)
        ScreenUtils.clear(1f, 0f, 0f, 1f)
        val dudeEntity = entities.find { it.hasComponent(SteamManPlayerComponent::class) && it.ownedBy == Entity.OwnedBy.Player(playerId) }
        val dudePosition = dudeEntity?.getComponent(PositionComponent::class)
        val x = dudePosition?.xPos ?: 0f
        val y = dudePosition?.yPos ?: 0f
        viewport.run {
            (camera as OrthographicCamera).zoom = 1f / SCALE
            camera.position.set(x, y, 0f)
            camera.update()
        }
        stage.draw()
    }

    private fun processEntities(entities: List<Entity>, playerId: PlayerId) {
        entities.forEach { entity ->
            var actor = entityActors[entity.id]
            if (actor == null) {
                actor = createActor(entity, playerId)
                entityActors[entity.id] = actor
                stage.addActor(actor)
            }
            updateActor(actor, entity, playerId)
        }
        cleanUpActors(entities)
    }

    private fun createActor(entity: Entity, playerId: PlayerId): PannaActor {
        val drawableComponent = entity.getComponent(DrawableComponent::class)
        return when (drawableComponent.drawStrategy) {
            // TODO abstract fucking factory
            CHARACTER_STRATEGY -> DudeActor(
                textures[TEXTURE_ID_DUDE_BLUE],
                textures[TEXTURE_ID_DUDE_BLUE_JUMPING],
                textures[TEXTURE_ID_EYES_BLUE_LOOKING_UP],
                textures[TEXTURE_ID_EYES_BLUE_LOOKING_STRAIGHT],
                textures[TEXTURE_ID_EYES_BLUE_LOOKING_DOWN],
            )
            SOLID_TERRAIN_TILE_STRATEGY -> SolidTerrainTileActor(textures[TEXTURE_ID_BROWN_BLOCK])
            BULLET_STRATEGY -> BulletActor(textures[TEXTURE_ID_BULLET])
            else -> {
                throw RuntimeException("Unknown draw strategy: ${drawableComponent.drawStrategy}")
            }
        }
    }

    private fun cleanUpActors(entities: List<Entity>) {
        val entitiesToRemove = entityActors.keys.filter { entityId ->
            entities.none { it.id == entityId }
        }
        entitiesToRemove.forEach { entityId ->
            entityActors.remove(entityId)?.remove()
        }
    }

    fun updateActor(actor: PannaActor, entity: Entity, playerId: PlayerId) {
        val positionComponent = entity.getComponent(PositionComponent::class)
        actor.x = positionComponent.xPos
        actor.y = positionComponent.yPos
        actor.update(entity)
    }
}
