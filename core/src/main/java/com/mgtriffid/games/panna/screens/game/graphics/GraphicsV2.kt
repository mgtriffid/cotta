package com.mgtriffid.games.panna.screens.game.graphics

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.panna.screens.game.SCALE
import com.mgtriffid.games.panna.screens.game.graphics.actors.ActorFactory
import com.mgtriffid.games.panna.screens.game.graphics.actors.PannaActor
import com.mgtriffid.games.panna.screens.game.graphics.textures.PannaTextures
import com.mgtriffid.games.panna.shared.BULLET_STRATEGY
import com.mgtriffid.games.panna.shared.CHARACTER_STRATEGY
import com.mgtriffid.games.panna.shared.SOLID_TERRAIN_TILE_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent

class GraphicsV2 {

    private lateinit var textures: PannaTextures
    private val actorFactory = ActorFactory()
    private lateinit var stage: Stage
    lateinit var viewport: Viewport
    private val entityActors = HashMap<EntityId, PannaActor>()

    fun initialize() {
        textures = PannaTextures()
        textures.init()
        actorFactory.initialize(textures)
        prepareStage()
        setCrosshairCursor()
    }

    private fun setCrosshairCursor() {
        val pm = Pixmap(Gdx.files.getFileHandle("panna/crosshair/crosshair.png", Files.FileType.Classpath))
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 7, 7))
    }

    private fun prepareStage() {
        viewport = ExtendViewport(960f, 960 * 9 / 16f)
        stage = Stage(viewport)
    }

    fun dispose() {
        stage.dispose()
        textures.dispose()
    }

    fun draw(state: DrawableState, playerId: PlayerId, delta: Float) {
        processEntities(state, playerId)
        stage.act(delta)
        ScreenUtils.clear(1f, 0f, 0f, 1f)
        val dudeEntity = state.entities.find { it.hasComponent(SteamManPlayerComponent::class) && it.ownedBy == Entity.OwnedBy.Player(playerId) }
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

    // GROOM 4 levels of indent is too many
    private fun processEntities(state: DrawableState, playerId: PlayerId) {
        state.entities.forEach { entity ->
            val id = entity.id
            var actor = entityActors[id]
            if (actor == null && id is AuthoritativeEntityId) {
                val predictedId = state.authoritativeToPredictedEntityIds[id]
                if (predictedId != null) {
                    actor = entityActors[predictedId]
                    if (actor != null) {
                        entityActors.remove(predictedId)
                        entityActors[id] = actor
                    }
                }
            }
            if (actor == null) {
                actor = createActor(entity, playerId)
                entityActors[id] = actor
                stage.addActor(actor.actor)
            }
            updateActor(actor, entity, playerId)
        }
        cleanUpActors(state.entities)
    }

    private fun createActor(entity: Entity, playerId: PlayerId): PannaActor {
        val drawableComponent = entity.getComponent(DrawableComponent::class)
        return when (drawableComponent.drawStrategy) {
            CHARACTER_STRATEGY -> actorFactory.createDude()
            SOLID_TERRAIN_TILE_STRATEGY -> actorFactory.createSolidTerrainTile()
            BULLET_STRATEGY -> actorFactory.createBullet()
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
            entityActors.remove(entityId)?.actor?.remove()
        }
    }

    private fun updateActor(actor: PannaActor, entity: Entity, playerId: PlayerId) {
        val positionComponent = entity.getComponent(PositionComponent::class)
        actor.actor.x = positionComponent.xPos
        actor.actor.y = positionComponent.yPos
        actor.update(entity)
    }
}
