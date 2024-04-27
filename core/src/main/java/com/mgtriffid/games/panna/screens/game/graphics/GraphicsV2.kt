package com.mgtriffid.games.panna.screens.game.graphics

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.codahale.metrics.MetricRegistry
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.panna.screens.game.SCALE
import com.mgtriffid.games.panna.screens.game.debug.MetricsDisplay
import com.mgtriffid.games.panna.screens.game.debug.toMetricStats
import com.mgtriffid.games.panna.screens.game.graphics.actors.ActorFactory
import com.mgtriffid.games.panna.screens.game.graphics.actors.PannaActor
import com.mgtriffid.games.panna.screens.game.graphics.textures.PannaTextures
import com.mgtriffid.games.panna.shared.BULLET_STRATEGY
import com.mgtriffid.games.panna.shared.CHARACTER_STRATEGY
import com.mgtriffid.games.panna.shared.SOLID_TERRAIN_TILE_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import com.mgtriffid.games.panna.shared.game.effects.visual.BulletHitsGroundVisualEffect
import com.mgtriffid.games.panna.shared.game.effects.visual.RailgunVisualEffect

class GraphicsV2 {

    private lateinit var textures: PannaTextures
    private val actorFactory = ActorFactory()
    private lateinit var stage: Stage
    lateinit var viewport: Viewport
    private val entityActors = HashMap<EntityId, PannaActor>()
    private lateinit var clickToJoinLabel: Label
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var metricsDisplay: MetricsDisplay

    fun initialize() {
        textures = PannaTextures()
        spriteBatch = SpriteBatch()
        textures.init()
        actorFactory.initialize(textures)
        prepareStage()
        setCrosshairCursor()
        prepareMetricsDisplay()
    }

    private fun setCrosshairCursor() {
        val pm = Pixmap(Gdx.files.getFileHandle("panna/crosshair/crosshair.png", Files.FileType.Classpath))
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 7, 7))
    }

    private fun prepareStage() {
        viewport = ExtendViewport(960f, 960 * 9 / 16f)
        stage = Stage(viewport, spriteBatch)

        clickToJoinLabel = createClickToJoinLabel()
        stage.addActor(clickToJoinLabel)
    }

    private fun prepareMetricsDisplay() {
        metricsDisplay = MetricsDisplay(spriteBatch)
    }

    fun dispose() {
        stage.dispose()
        textures.dispose()
        metricsDisplay.dispose()
    }

    fun draw(
        state: DrawableState.Ready,
        delta: Float,
        mayJoin: Boolean,
        metrics: MetricRegistry
    ) {
        processEntities(state)
        processEffects(state)
        stage.act(delta)
        ScreenUtils.clear(1f, 0f, 0f, 1f)
        val dudeEntity = state.entities.find {
            it.hasComponent(SteamManPlayerComponent::class) && it.ownedBy == Entity.OwnedBy.Player(state.playerId)
        }
        val dudePosition = dudeEntity?.getComponent(PositionComponent::class)
        val x = dudePosition?.xPos ?: 0f
        val y = dudePosition?.yPos ?: 0f
        viewport.run {
            (camera as OrthographicCamera).zoom = 1f / SCALE
            camera.position.set(x, y, 0f)
            camera.update()
        }
        stage.draw()
        metricsDisplay.updateBufferLength(metrics.histogram("buffer_ahead").toMetricStats())
        metricsDisplay.updateSentChunkSize(metrics.histogram("sent_chunk_size").toMetricStats())
        metricsDisplay.updateServerBufferLength(metrics.histogram("server_buffer_ahead").toMetricStats())
        metricsDisplay.stage.act()
        metricsDisplay.stage.draw()
    }

    // GROOM 4 levels of indent is too many
    private fun processEntities(state: DrawableState.Ready) {
        state.entities.forEach { entity ->
            val id = entity.id
            var actor = entityActors[id]
            if (actor == null) {
                actor = createActor(entity, state.playerId)
                entityActors[id] = actor
                stage.addActor(actor.actor)
            }
            updateActor(actor, entity, state.playerId)
        }
        cleanUpActors(state.entities)
    }

    private fun processEffects(state: DrawableState.Ready) {
        (state.effects.predicted.map { it.effect } + state.effects.real.map { it.effect }).forEach { effect ->
            when (effect) {
                is BulletHitsGroundVisualEffect -> {
                    val actor = actorFactory.createBulletHitsGroundVisualEffect()
                    actor.x = effect.x
                    actor.y = effect.y
                    actor.addAction(
                        Actions.sequence(
                            Actions.delay(0.4f),
                            Actions.removeActor()
                        )
                    )
                    stage.addActor(actor)
                }
                is RailgunVisualEffect -> {
                    val actor = actorFactory.createRailgunVisualEffect(
                        effect.x1,
                        effect.y1,
                        effect.x2,
                        effect.y2
                    )
                    actor.addAction(
                        Actions.sequence(
                            Actions.delay(0.3f),
                            Actions.removeActor()
                        )
                    )
                    stage.addActor(actor)
                }
            }
        }
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

    private fun createClickToJoinLabel(): Label {
        return Label("Click anywhere to join", LabelStyle(BitmapFont(), Color.GREEN))
    }
}
