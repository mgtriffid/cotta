package com.mgtriffid.games.panna.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientFactory
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.utils.now
import com.mgtriffid.games.panna.PannaClientGdxInput
import com.mgtriffid.games.panna.PannaConfigStatic.height
import com.mgtriffid.games.panna.PannaConfigStatic.width
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.PannaGraphicsConfig
import com.mgtriffid.games.panna.graphics.textures.PannaTextures
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import com.mgtriffid.games.panna.shared.lobby.PannaGame
import mu.KotlinLogging
import space.earlygrey.shapedrawer.ShapeDrawer
import kotlin.math.roundToInt

const val SCALE = 3

private val logger = KotlinLogging.logger {}

// TODO handle resize and pause and all the things
class GameScreen(
    private val gdxGame: PannaGdxGame
) : ScreenAdapter() {
    private lateinit var cottaClient: CottaClient

    private val graphicsConfig: PannaGraphicsConfig = PannaGraphicsConfig()

    private lateinit var debuggingTexture: Texture

    private lateinit var drawer: ShapeDrawer
    lateinit var batch: SpriteBatch

    lateinit var textures: PannaTextures
    lateinit var camera: OrthographicCamera

    private var nextTickAt: Long = -1
    private var tickLength: Long = -1

    private lateinit var input: PannaClientGdxInput

    override fun show() {
        batch = SpriteBatch()
        initializeShapeDrawer()
        textures = PannaTextures()
        textures.init()
        camera = OrthographicCamera(960f, 960 * 9 / 16f)
        input = PannaClientGdxInput()
        val game = PannaGame()
        logger.debug { "Tick length is ${game.config.tickLength}" }
        tickLength = game.config.tickLength
        cottaClient = CottaClientFactory().create(game, input)
        cottaClient.initialize()
        nextTickAt = now()
    }

    /**
     * This is called rapidly by LibGDX game loop. Think of this as of the main loop body.
     */
    override fun render(delta: Float) {
        logger.trace { "${GameScreen::class.simpleName}#render called" }

        input.accumulate()

        var tickHappened = false
        val now = now()
        if (nextTickAt <= now) {
            cottaClient.tick()
            nextTickAt += tickLength
            tickHappened = true
        }

        if (tickHappened) {
            input.clear()
        }

        draw(1.0f - (nextTickAt - now).toFloat() / tickLength.toFloat())
    }

    private fun updateCamera(xPos: Float?, yPos: Float?) {
        val mouseX = Gdx.input.x
        val mouseY = Gdx.input.y
        // position camera so that middle of line between mouse and dude is in the middle of the screen:
        val cameraX = (xPos ?: 0f) * SCALE + (mouseX - width / 2)
        val cameraY = (yPos ?: 0f) * SCALE + (height / 2 - mouseY)
        camera.position.set(cameraX, cameraY, 0f)
        camera.update()
        batch.projectionMatrix = camera.combined
    }

    override fun dispose() {
        batch.dispose()
        debuggingTexture.dispose()
        textures.dispose()
    }

    private fun draw(alpha: Float) {
        val entities = getDrawableEntities(alpha)
        val dudePosition = entities.find {
            it.hasComponent(SteamManPlayerComponent::class) &&
                it.ownedBy == Entity.OwnedBy.Player(cottaClient.localPlayer.playerId)
        }?.getComponent(PositionComponent::class)
        updateCamera(dudePosition?.xPos, dudePosition?.yPos)
        beginDraw()
        drawEntities(entities, alpha)
        endDraw()
    }

    private fun drawEntities(entities: List<Entity>, alpha: Float) {
        logger.debug { "Drawing entities, alpha = $alpha" }
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

    private fun drawTextureBounds(position: PositionComponent, texture: TextureRegion) {
        drawer.rectangle(
            position.xPos * SCALE - texture.regionWidth * SCALE / 2,
            position.yPos * SCALE - texture.regionHeight * SCALE / 2,
            texture.regionWidth * SCALE.toFloat(),
            texture.regionHeight * SCALE.toFloat(),
            Color.WHITE
        )
    }

    private fun getDrawableEntities(alpha: Float): List<Entity> {
        return cottaClient.getDrawableEntities(alpha, DrawableComponent::class, PositionComponent::class)
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

    // <editor-fold desc="Draw lifecycle">
    private fun endDraw() {
        batch.end()
    }

    private fun beginDraw() {
        ScreenUtils.clear(1f, 0f, 0f, 1f)
        batch.begin()
    }
    // </editor-fold>

}
