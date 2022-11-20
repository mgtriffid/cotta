package com.mgtriffid.games.panna.screens.menu.components.characterlist

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Container
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.mgtriffid.games.panna.screens.menu.MenuScreen
import com.mgtriffid.games.panna.screens.menu.MenuTextures
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CharacterListWindow(
    private val characterListModel: CharacterListModel,
    private val menuTextures: MenuTextures
) {
    val window: Window = Window(
        "Characters list", Window.WindowStyle(
            BitmapFont(),
            Color.WHITE,
            null
        )
    )
    val cells = ArrayList<Container<Stack>>()
    val selectionFrames = ArrayList<SelectionFrame>()

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
        for (i in 0..3) {
            val cell = createCell()
            charactersTable.add(cell)
            cells.add(cell)
        }
        charactersTable.row()
        for (i in 4..7) {
            val cell = createCell()
            charactersTable.add(cell)
            cells.add(cell)
        }
        registerModelListeners()
        logger.debug { "Listener onUpdateCharacters configured" }
    }

    private fun registerModelListeners() {
        characterListModel.onUpdateCharacters = { characters -> updateCharactersInTable(characters) }
        characterListModel.onUpdateSelectedCharacterIndex = { before, after ->
            logger.debug { "onUpdateSelectedCharacterIndex listener invoked, before = $before, after = $after, selectionFrames.size = ${selectionFrames.size}" }
            before?.let { selectionFrames.getOrNull(it)?.hide() }
            after?.let { selectionFrames.getOrNull(it)?.show() }
        }
    }

    private fun updateCharactersInTable(characters: List<CharacterModel>) {
        logger.debug { "Updating characters table with ${characters.size} characters" }
        // for cells that correspond to these characters, first update drawable
        selectionFrames.clear()
        characters.forEachIndexed { index, character ->
            val cell = cells[index]
            cell.actor?.addAction(Actions.removeActor())
            val stack = Stack()
            cell.actor = stack
            val imageButton = ImageButton(TextureRegionDrawable(menuTextures.character))
            imageButton.addListener(object: ClickListener() {
                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    logger.debug { "Cell $index clicked" }
                    super.touchUp(event, x, y, pointer, button)
                    if (button == Input.Buttons.LEFT) {
                        characterListModel.selectedCharacterIndex = index
                    }
                }

            })
            stack.add(imageButton)
            val selectionFrame = SelectionFrame(menuTextures.selection)
            selectionFrames.add(selectionFrame)
            stack.add(selectionFrame.overlay)
        }
        for (index in characters.size..7) {
            val cell = cells[index]
            cell.actor?.addAction(Actions.removeActor())
        }
    }

    private fun createCell(): Container<Stack> {
        val container = Container<Stack>()
        container.width(MenuScreen.UiConfig.CHARACTER_CELL_WIDTH)
        container.height(MenuScreen.UiConfig.CHARACTER_CELL_HEIGHT)
        val stack = Stack()
        container.actor = stack
        val imageButton = ImageButton(TextureRegionDrawable(menuTextures.character))
        imageButton.width = 30f
        imageButton.height = 30f
        stack.add(imageButton)
        return container
    }

}
