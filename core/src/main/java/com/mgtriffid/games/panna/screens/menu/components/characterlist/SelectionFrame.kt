package com.mgtriffid.games.panna.screens.menu.components.characterlist

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table

class SelectionFrame(selectionTexture: Texture) {
    private val frameImage = Image(selectionTexture)
    val overlay = Table()

    init {
        frameImage.isVisible = false
        overlay.add(frameImage).bottom().left()
    }

    fun hide() {
        frameImage.isVisible = false
    }
    fun show() {
        frameImage.isVisible = true
    }
}
