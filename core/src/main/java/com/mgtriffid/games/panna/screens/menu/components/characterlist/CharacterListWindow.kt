package com.mgtriffid.games.panna.screens.menu.components.characterlist

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.mgtriffid.games.panna.screens.menu.MenuScreen

class CharacterListWindow(
    private val characterListModel: CharacterListModel
) {
    val window: Window = Window(
        "Characters list", Window.WindowStyle(
            BitmapFont(),
            Color.WHITE,
            null
        )
    )
    val cells = ArrayList<Container<Stack>>()

    init {
        window.debug = true
        window.isMovable = false
        window.padTop(20f)
        window.isResizable = false
        window.setSize(MenuScreen.UiConfig.CHARACTERS_TABLE_WIDTH, MenuScreen.UiConfig.CHARACTERS_TABLE_HEIGHT)
        window.isMovable = false
        window.setPosition(240f, 135f)
        val charactersTable = Table()
        charactersTable.debug = true
        charactersTable.pad(5f)
        window.add(charactersTable)
        repeat(4) {
            val cell = createCell()
            charactersTable.add(cell)
            cells.add(cell)
        }
        charactersTable.row()
        repeat(4) {
            val cell = createCell()
            charactersTable.add(cell)
            cells.add(cell)
        }
    }

    private fun createCell(): Container<Stack> {
        val container = Container<Stack>()
        container.width(MenuScreen.UiConfig.CHARACTER_CELL_WIDTH)
        container.height(MenuScreen.UiConfig.CHARACTER_CELL_HEIGHT)
        val stack = Stack()
        container.actor = stack
        return container
    }
}
