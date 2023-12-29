package com.mgtriffid.games.panna.screens.menu

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientFactory
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.utils.now
import com.mgtriffid.games.panna.PannaClientGdxInput
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.graphics.textures.PannaTextures
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.lobby.PannaGame
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

// TODO handle resize and pause and all the things
class GameScreen(
    private val gdxGame: PannaGdxGame
) : ScreenAdapter() {
    private lateinit var cottaClient: CottaClient

    lateinit var batch: SpriteBatch
    lateinit var img: Texture

    lateinit var textures: PannaTextures

    private var nextTickAt: Long = -1
    private var tickLength: Long = -1

    private lateinit var input: PannaClientGdxInput

    private val debugPositions = mutableMapOf<EntityId, Pair<Int, Int>>()

    override fun show() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        textures = PannaTextures()
        textures.init()
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
        if (nextTickAt <= now()) {
            cottaClient.tick()
            nextTickAt += tickLength
            tickHappened = true
        }

        if (tickHappened) {
            input.clear()
        }

        draw()
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }

    private fun draw() {
        beginDraw()
        drawEntities()
        endDraw()
    }

    private fun drawEntities() {
        getDrawableEntities().forEach {
            val drawable = it.getComponent(DrawableComponent::class)
            val position = it.getComponent(PositionComponent::class)
            logger.debug { "Drawing entity ${it.id} owned by ${it.ownedBy}. Position: $position." }
            val texture = textures[drawable.textureId]
            logPositionIfChanged(it, position)
            batch.draw(texture, position.xPos.toFloat(), position.yPos.toFloat())
        }
    }

    private fun getDrawableEntities(): List<Entity> {
        return cottaClient.getDrawableEntities(DrawableComponent::class, PositionComponent::class)
    }

    private fun logPositionIfChanged(entity: Entity, position: PositionComponent) {
        val recorded = debugPositions[entity.id]
        if (recorded != Pair(position.xPos, position.yPos)) {
            logger.debug { "Entity ${entity.id} moved to ${position.xPos}, ${position.yPos}" }
            debugPositions[entity.id] = Pair(position.xPos, position.yPos)
        }
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
