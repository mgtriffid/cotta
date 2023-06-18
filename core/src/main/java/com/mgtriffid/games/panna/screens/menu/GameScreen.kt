package com.mgtriffid.games.panna.screens.menu

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.client.impl.CottaClientImpl
import com.mgtriffid.games.cotta.core.TICK_LENGTH
import com.mgtriffid.games.cotta.core.impl.CottaEngineImpl
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaNetwork
import com.mgtriffid.games.cotta.utils.now
import com.mgtriffid.games.panna.PannaClientGdxInput
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.graphics.textures.PannaTextures
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.lobby.PannaGame
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
// TODO handle resize and pause and all the things
class GameScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {
    private lateinit var cottaClient: CottaClient

    lateinit var batch: SpriteBatch
    lateinit var img: Texture

    lateinit var textures: PannaTextures

    private var nextTickAt: Long = -1

    override fun show() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        val engine  = CottaEngineImpl()
        textures = PannaTextures()
        textures.init()

        cottaClient = CottaClient.getInstance(
            game = PannaGame(),
            engine = engine,
            network = KryonetCottaNetwork().createClientNetwork(),
            input = PannaClientGdxInput()
        )
        cottaClient.initialize()
        nextTickAt = now()
    }

    override fun render(delta: Float) {
        logger.debug { "${GameScreen::class.simpleName}#render called" }

//        accumulateInput()

        if (nextTickAt <= now()) {
            cottaClient.tick()
            nextTickAt += TICK_LENGTH
        }

        actuallyDraw()
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }

    private fun actuallyDraw() {
        beginDraw()
        drawEntities()
        endDraw()
    }

    private fun drawEntities() {
        getDrawableEntities().forEach {
            logger.debug { "Drawing entity $it" }
            val drawable = it.getComponent(DrawableComponent::class)
            val position = it.getComponent(PositionComponent::class)
            val texture = textures[drawable.textureId]
            batch.draw(texture, position.xPos.toFloat(), position.yPos.toFloat())
        }
    }

    private fun getDrawableEntities() = (cottaClient as CottaClientImpl<*, *>).cottaState.entities().all().filter {
        it.hasComponent(DrawableComponent::class) && it.hasComponent(PositionComponent::class)
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
