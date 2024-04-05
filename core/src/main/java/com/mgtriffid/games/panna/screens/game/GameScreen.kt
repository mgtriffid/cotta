package com.mgtriffid.games.panna.screens.game

import com.badlogic.gdx.ScreenAdapter
import com.mgtriffid.games.cotta.client.DrawableState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.gdx.CottaGdxAdapter
import com.mgtriffid.games.panna.PannaClientGdxInput
import com.mgtriffid.games.panna.PannaGdxGame
import com.mgtriffid.games.panna.screens.game.graphics.GraphicsV2
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.PannaGame
import com.mgtriffid.games.panna.shared.game.components.CharacterInputComponent2
import mu.KotlinLogging

const val SCALE = 3

private val logger = KotlinLogging.logger {}

// TODO handle resize and pause and all the things
class GameScreen(
    private val gdxGame: PannaGdxGame
) : ScreenAdapter() {
    private val drawableComponents = listOf(
        DrawableComponent::class,
        PositionComponent::class
    )

    private val graphics: GraphicsV2 = GraphicsV2()
    private lateinit var gdxAdapter: CottaGdxAdapter

    private lateinit var input: PannaClientGdxInput

    override fun show() {
        graphics.initialize()
        input = PannaClientGdxInput(graphics.viewport)
        val game = PannaGame()
        gdxAdapter = CottaGdxAdapter(game, input)
        gdxAdapter.initialize()
    }

    /**
     * This is called rapidly by LibGDX game loop. Think of this as of the main loop body.
     */
    override fun render(delta: Float) {
        logger.trace { "${GameScreen::class.simpleName}#render called" }
        draw(gdxAdapter(), delta)
    }

    override fun dispose() {
        graphics.dispose()
    }

    private fun draw(alpha: Float, delta: Float) {
        when (val state = getDrawableState(alpha)) {
            DrawableState.NotReady -> return
            is DrawableState.Ready -> {
                if (noDude(state)) {
                    input.mayJoin = true
                }
                graphics.draw(state, delta, input.mayJoin, gdxAdapter.metrics())
            }
        }
    }

    private fun noDude(state: DrawableState.Ready) : Boolean {
        return state.entities.none {
            it.hasComponent(CharacterInputComponent2::class) &&
                it.ownedBy == Entity.OwnedBy.Player(state.playerId)
        }
    }

    private fun getDrawableState(alpha: Float): DrawableState {
        return gdxAdapter.getDrawableState(alpha, drawableComponents)
    }
}
