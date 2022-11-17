package com.mgtriffid.games.panna.screens.menu.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.mgtriffid.games.panna.PannaConfig
import com.mgtriffid.games.panna.screens.menu.MenuScreen
import com.mgtriffid.games.panna.screens.menu.MenuState
import com.mgtriffid.games.panna.screens.menu.MenuTextures
import com.mgtriffid.games.panna.screens.menu.UI_DEBUG

class StatusDialogWindow(
    val menuState: MenuState, val textures: MenuTextures, val config: PannaConfig
) {
    val statusPanelWindow: Window

    init {
        statusPanelWindow = Window(
            "", Window.WindowStyle(
                BitmapFont(), Color.WHITE, TextureRegionDrawable(textures.statusPanelBackground)
            )
        )
        statusPanelWindow.titleTable.isVisible = false
        statusPanelWindow.debug = UI_DEBUG
        statusPanelWindow.setPosition(
            config.width.toFloat() / 2 - MenuScreen.UiConfig.STATUS_PANEL_WIDTH / 2,
            config.height.toFloat() / 2 - MenuScreen.UiConfig.STATUS_PANEL_HEIGHT / 2
        )
        statusPanelWindow.setSize(MenuScreen.UiConfig.STATUS_PANEL_WIDTH, MenuScreen.UiConfig.STATUS_PANEL_HEIGHT)
        addStatusText()
        val stack = Stack()
        addDialogOkayTextButton(stack)
        addDialogCancelTextButton(stack)
        statusPanelWindow.row()
        statusPanelWindow.add(stack)
    }

    private fun addStatusText() {
        val statusPanelTextLabel = Label("neuzhto", MenuScreen.Styles.formInputLabelStyle)
        statusPanelWindow.add(statusPanelTextLabel).expandX()

        val setVisible: (Boolean) -> Unit = { value -> statusPanelWindow.isVisible = value }
        val setText: (String) -> Unit = { text -> statusPanelTextLabel.setText(text) }
        val value = object : Action() {
            override fun act(delta: Float): Boolean {
                when (menuState.state) {
                    MenuScreen.State.IDLE -> {
                        setVisible(false)
                    }

                    MenuScreen.State.AUTHORIZATION -> {
                        setVisible(true)
                        setText("Authorization...")
                    }

                    MenuScreen.State.RETRIEVING_CHARACTER_LIST -> {
                        setVisible(true)
                        setText("Retrieving character list...")
                    }

                    MenuScreen.State.RETRIEVED_CHARACTER_LIST -> {
                        setVisible(false)
                    }

                    MenuScreen.State.FAILED_TO_RETRIEVE_CHARACTERS_LIST -> {
                        setVisible(true)
                        setText("Failed to retrieve characters list")
                    }

                    MenuScreen.State.AUTHORIZATION_FAILED -> {
                        setVisible(true)
                        setText("Authorization failed")
                    }
                }
                return false
            }
        }
        statusPanelTextLabel.addAction(value)
    }

    private fun addDialogOkayTextButton(stack: Stack) {
        val buttonStyle = getDialogButtonStyle()

        val textButton = TextButton("Okay", buttonStyle)
        stack.add(textButton)
        val setVisible: (Boolean) -> Unit = { value -> textButton.isVisible = value }
        val setText: (String) -> Unit = { text -> textButton.setText(text) }
        textButton.addAction(object : Action() {
            override fun act(delta: Float): Boolean {
                when (menuState.state) {
                    MenuScreen.State.IDLE -> {
                        setVisible(false)
                    }

                    MenuScreen.State.AUTHORIZATION -> {
                        setVisible(false)
                    }

                    MenuScreen.State.RETRIEVING_CHARACTER_LIST -> {
                        setVisible(false)
                    }

                    MenuScreen.State.RETRIEVED_CHARACTER_LIST -> {
                        setVisible(false)
                    }

                    MenuScreen.State.FAILED_TO_RETRIEVE_CHARACTERS_LIST -> {
                        setVisible(true)
                    }

                    MenuScreen.State.AUTHORIZATION_FAILED -> {
                        setVisible(true)
                    }
                }
                return false
            }
        })
        textButton.addListener(object : ClickListener() {
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                menuState.idle()
            }
        })
    }

    private fun addDialogCancelTextButton(stack: Stack) {
        val buttonStyle = getDialogButtonStyle()

        val textButton = TextButton("Cancel", buttonStyle)
        stack.add(textButton)
        val setVisible: (Boolean) -> Unit = { value -> textButton.isVisible = value }
        textButton.addAction(object : Action() {
            override fun act(delta: Float): Boolean {
                when (menuState.state) {
                    MenuScreen.State.IDLE -> {
                        setVisible(false)
                    }

                    MenuScreen.State.AUTHORIZATION -> {
                        setVisible(true)
                    }

                    MenuScreen.State.RETRIEVING_CHARACTER_LIST -> {
                        setVisible(true)
                    }

                    MenuScreen.State.RETRIEVED_CHARACTER_LIST -> {
                        setVisible(false)
                    }

                    MenuScreen.State.FAILED_TO_RETRIEVE_CHARACTERS_LIST -> {
                        setVisible(false)
                    }

                    MenuScreen.State.AUTHORIZATION_FAILED -> {
                        setVisible(false)
                    }
                }
                return false
            }
        })
    }

    private fun getDialogButtonStyle(): TextButton.TextButtonStyle {
        return getButtonStyle(
            upTexture = textures.dialogButtonUpTexture,
            downTexture = textures.dialogButtonDownTexture
        )
    }

}
