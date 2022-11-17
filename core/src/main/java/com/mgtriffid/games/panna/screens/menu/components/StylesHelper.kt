package com.mgtriffid.games.panna.screens.menu.components

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.mgtriffid.games.panna.screens.menu.MenuScreen

fun getButtonStyle(upTexture: Texture, downTexture: Texture): TextButton.TextButtonStyle {
    val upRegion = TextureRegion(upTexture)
    val downRegion = TextureRegion(downTexture)
    val buttonStyle = TextButton.TextButtonStyle()
    buttonStyle.up = TextureRegionDrawable(upRegion)
    buttonStyle.down = TextureRegionDrawable(downRegion)
    buttonStyle.font = MenuScreen.Styles.formInputLabelStyle.font
    return buttonStyle
}
