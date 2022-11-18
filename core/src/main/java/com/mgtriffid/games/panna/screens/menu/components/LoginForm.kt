package com.mgtriffid.games.panna.screens.menu.components

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.mgtriffid.games.panna.screens.menu.MenuScreen
import com.mgtriffid.games.panna.screens.menu.MenuTextures
import com.mgtriffid.games.panna.screens.menu.UI_DEBUG
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LoginForm(
    private val textures: MenuTextures
) {
    val table: Table = Table()
    val passwordInput: TextField
    val loginInput: TextField
    var onClick: LoginForm.() -> Unit = {}
    init {
        table.setFillParent(true)
            table.debug = UI_DEBUG
            val loginLabel = Label("login", MenuScreen.Styles.formInputLabelStyle)
            table.add(loginLabel)
            // TODO use pref size
            val textFieldStyle = TextField.TextFieldStyle(
                BitmapFont(),
                Color.YELLOW,
                TextureRegionDrawable(textures.textInputCursor),
                null,
                NinePatchDrawable(
                    NinePatch(
                        textures.textInput9Patch,
                        8, 8, 8, 8
                    )
                )
            )
            loginInput = TextField(
                "", textFieldStyle
            )
            table.add(loginInput).width(300f).height(50f).pad(10f)
            table.row()
            val passwordLabel = Label("password", MenuScreen.Styles.formInputLabelStyle)
            table.add(passwordLabel)
            passwordInput = TextField(
                "", textFieldStyle
            )
            passwordInput.isPasswordMode = true
            passwordInput.setPasswordCharacter('*')
            table.add(passwordInput).width(300f).height(50f).pad(10f)
            table.row()
            val buttonStyle = getLoginButtonStyle()
            val loginButton = TextButton("login", buttonStyle)
            loginButton.addListener(object : ClickListener() {
                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    logger.debug { "Login button clicked" }
                    super.touchUp(event, x, y, pointer, button)
                    if (button == Input.Buttons.LEFT) onClick()
                    logger.debug { "touchUp processing of loginButton complete" }
                }
            })

            table.add(loginButton).colspan(2)

    }
    private fun getLoginButtonStyle(): TextButton.TextButtonStyle {
        return getButtonStyle(
            upTexture = textures.loginButtonUpTexture,
            downTexture = textures.loginButtonDownTexture
        )
    }
}
