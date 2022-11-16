package com.mgtriffid.games.panna.screens.menu

import com.badlogic.gdx.graphics.Texture

/**
 * Constructor is expensive!
 */

class MenuTextures {
    val background = Texture("menu_bg.png")
    val statusPanelBackground = Texture("status_panel_bg.png")
    val loginButtonUpTexture = Texture("blue_button_up.png")
    val loginButtonDownTexture = Texture("blue_button_down.png")
    val dialogButtonUpTexture = Texture("red_button_up.png")
    val dialogButtonDownTexture = Texture("red_button_down.png")
    val textInputCursor = Texture("cursor.png")
    val textInput9Patch = Texture("textfield_9patch_atlas.png")

    fun dispose() {
        background.dispose()
        statusPanelBackground.dispose()
        loginButtonUpTexture.dispose()
        loginButtonDownTexture.dispose()
        dialogButtonUpTexture.dispose()
        dialogButtonDownTexture.dispose()
        textInputCursor.dispose()
        textInput9Patch.dispose()
    }
}
