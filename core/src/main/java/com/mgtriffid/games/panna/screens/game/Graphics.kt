package com.mgtriffid.games.panna.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.panna.PannaConfigStatic
import com.mgtriffid.games.panna.PannaGraphicsConfig
import com.mgtriffid.games.panna.graphics.textures.PannaTextures
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import mu.KotlinLogging
import space.earlygrey.shapedrawer.ShapeDrawer
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

const val centerAtPlayer = true

class Graphics {

    private lateinit var batch: SpriteBatch
    private lateinit var textures: PannaTextures
    private lateinit var camera: OrthographicCamera
    private lateinit var debuggingTexture: Texture
    private lateinit var drawer: ShapeDrawer
    private val graphicsConfig: PannaGraphicsConfig = PannaGraphicsConfig()

    fun initialize() {
        batch = SpriteBatch()
        initializeShapeDrawer()
        textures = PannaTextures()
        textures.init()
        camera = OrthographicCamera(960f, 960 * 9 / 16f)
    }

    private fun initializeShapeDrawer() {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.drawPixel(0, 0)
        debuggingTexture = Texture(pixmap) //remember to dispose of later

        pixmap.dispose()
        val region = TextureRegion(debuggingTexture, 0, 0, 1, 1)
        drawer = ShapeDrawer(batch, region)
    }

    fun draw(
        entities: List<Entity>,
        playerId: PlayerId
    ) {
        val dudePosition = entities.find {
            it.hasComponent(SteamManPlayerComponent::class) &&
                it.ownedBy == Entity.OwnedBy.Player(playerId)
        }?.getComponent(PositionComponent::class)
        updateCamera(dudePosition?.xPos, dudePosition?.yPos)
        beginDraw()
        drawEntities(entities)
        endDraw()
    }

    private fun drawEntities(entities: List<Entity>) {
        entities.forEach {
            val drawable = it.getComponent(DrawableComponent::class)
            val position = it.getComponent(PositionComponent::class)
            logger.debug { "Drawing entity ${it.id} owned by ${it.ownedBy}. Position: $position." }
            val texture = TextureRegion(textures[drawable.textureId])
            batch.draw(
                texture,
                (position.xPos.roundToInt() * SCALE).toFloat() - (texture.regionWidth * SCALE / 2),
                (position.yPos.roundToInt() * SCALE).toFloat() - (texture.regionHeight * SCALE / 2),
                0f, 0f,
                texture.regionWidth.toFloat(), texture.regionHeight.toFloat(),
                SCALE.toFloat(), SCALE.toFloat(), 0f
            )
            drawDebugLines(position, texture)
        }
    }

    private fun drawDebugLines(position: PositionComponent, texture: TextureRegion) {
        if (graphicsConfig.showTextureBounds) {
            drawTextureBounds(position, texture)
        }
        if (graphicsConfig.showPosition) {
            drawPosition(position)
        }
    }

    private fun drawTextureBounds(position: PositionComponent, texture: TextureRegion) {
        drawer.rectangle(
            position.xPos * SCALE - texture.regionWidth * SCALE / 2,
            position.yPos * SCALE - texture.regionHeight * SCALE / 2,
            texture.regionWidth * SCALE.toFloat(),
            texture.regionHeight * SCALE.toFloat(),
            Color.WHITE
        )
    }

    private fun drawPosition(position: PositionComponent) {
        drawer.filledRectangle(
            position.xPos * SCALE - 1.5f, position.yPos * SCALE,
            3f, 1f,
            Color.WHITE
        )
        drawer.filledRectangle(
            position.xPos * SCALE, position.yPos * SCALE - 1.5f,
            1f, 3f,
            Color.WHITE
        )
    }

    private fun endDraw() {
        batch.end()
    }

    private fun beginDraw() {
        ScreenUtils.clear(1f, 0f, 0f, 1f)
        batch.begin()
    }

    fun dispose() {
        batch.dispose()
        debuggingTexture.dispose()
        textures.dispose()
    }

    private fun updateCamera(xPos: Float?, yPos: Float?) {
        if (centerAtPlayer) {
            camera.position.set((xPos ?: 0f) * SCALE, (yPos ?: 0f) * SCALE, 0f)
        } else {
            val mouseX = Gdx.input.x
            val mouseY = Gdx.input.y
            // position camera so that middle of line between mouse and dude is in the middle of the screen:
            val cameraX = (xPos ?: 0f) * SCALE + (mouseX - PannaConfigStatic.width / 2)
            val cameraY = (yPos ?: 0f) * SCALE + (PannaConfigStatic.height / 2 - mouseY)
            camera.position.set(cameraX, cameraY, 0f)
        }
        camera.update()
        batch.projectionMatrix = camera.combined
    }
}