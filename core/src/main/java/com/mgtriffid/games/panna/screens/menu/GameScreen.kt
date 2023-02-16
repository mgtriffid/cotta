package com.mgtriffid.games.panna.screens.menu

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.core.TICK_LENGTH
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaNetwork
import com.mgtriffid.games.panna.PannaGdxGame
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
// TODO handle resize and pause and all the things
class GameScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {
    private lateinit var cottaGame: CottaClient

    var batch: SpriteBatch? = null
    lateinit var img: Texture

    private var nextTickAt: Long = -1

    override fun show() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")

        cottaGame = CottaClient.getInstance(KryonetCottaNetwork().createClientNetwork())
        nextTickAt = now()
    }

    override fun render(delta: Float) {
        logger.debug { "${GameScreen::class.simpleName}#render called" }
        actuallyDraw()

        if (nextTickAt <= now()) {
            cottaGame.tick()
            nextTickAt += TICK_LENGTH
        }
    }

    override fun dispose() {
        batch!!.dispose()
        img.dispose()
    }

    private fun actuallyDraw() {
        ScreenUtils.clear(1f, 0f, 0f, 1f)
        batch!!.begin()
        batch!!.draw(img, 0f, 0f)
        batch!!.end()
    }
}

private fun now() = System.currentTimeMillis()
