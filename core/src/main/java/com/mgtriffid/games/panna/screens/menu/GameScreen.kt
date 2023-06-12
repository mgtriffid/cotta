package com.mgtriffid.games.panna.screens.menu

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.core.TICK_LENGTH
import com.mgtriffid.games.cotta.core.impl.CottaEngineImpl
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaNetwork
import com.mgtriffid.games.cotta.utils.now
import com.mgtriffid.games.panna.PannaClientGdxInput
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.shared.lobby.PannaGame
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
// TODO handle resize and pause and all the things
class GameScreen(
    private val game: PannaGdxGame
) : ScreenAdapter() {
    private lateinit var cottaClient: CottaClient

    var batch: SpriteBatch? = null
    lateinit var img: Texture

    private var nextTickAt: Long = -1

    override fun show() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        val engine  = CottaEngineImpl()

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
