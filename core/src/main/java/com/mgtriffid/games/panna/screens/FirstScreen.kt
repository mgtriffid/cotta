package com.mgtriffid.games.panna.screens

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import com.mgtriffid.games.panna.PannaGdxGame

class FirstScreen(
    val game: PannaGdxGame
) : ScreenAdapter() {

    var batch: SpriteBatch? = null
    lateinit var img: Texture

    override fun show() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
    }

    override fun render(delta: Float) {
        // called repeatedly so here we should call an adapter that could make client update when needed and render
        // also when needed. But we don't know how responsibilities are shared in client code. Client is user interface
        // plus state, not just state, so it's unfair to just decide all for client. Besides there is "predicted state"
        // which also needs to be taken into account. Overall, we may not decide for users what they need here. But
        // we may decide to expose just one thing: a method that has to be called repeatedly, as often as possible, and
        // which will first take care of real state, then of state plus predicted state, then finally of interpolated
        // state that is useful for rendering. Something like that. Perhaps after we introduce some graphics it will be
        // easier to develop something that would satisfy needs of most of LibGDX-oriented game developers.
        ScreenUtils.clear(1f, 0f, 0f, 1f)
        batch!!.begin()
        batch!!.draw(img, 0f, 0f)
        batch!!.end()
    }

    override fun dispose() {
        batch!!.dispose()
        img.dispose()
    }

}
