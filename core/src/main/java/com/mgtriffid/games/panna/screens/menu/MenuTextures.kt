package com.mgtriffid.games.panna.screens.menu

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

/**
 * Constructor is expensive!
 */

class MenuTextures {
    val selection = createTexture("selection.png")
    val character = createTexture("character.png")
    val xButtonUp = createTexture("x_button_up.png")
    val xButtonDown = createTexture("x_button_down.png")
    val background = createTexture("menu_bg.png")
    val statusPanelBackground = createTexture("status_panel_bg.png")
    val loginButtonUpTexture = createTexture("blue_button_up.png")
    val loginButtonDownTexture = createTexture("blue_button_down.png")
    val dialogButtonUpTexture = createTexture("red_button_up.png")
    val dialogButtonDownTexture = createTexture("red_button_down.png")
    val textInputCursor = createTexture("cursor.png")
    val textInput9Patch = createTexture("textfield_9patch_atlas.png")

    private val textures = ArrayList<Texture>()

    fun dispose() {
        textures.forEach { it.dispose() }
    }

    private fun createTexture(internalPath: String): Texture {
        return Texture(internalPath)
    }
}
