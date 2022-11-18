package com.mgtriffid.games.panna.screens.menu.components.characterlist

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CharacterListModel {
    var characters = emptyList<CharacterModel>()

    var selectedCharacterIndex: Int? = 0
        set(value) {
            val before = field
            field = value
            logger.debug { "selectedCharacterIndex updated to $value" }
            onUpdateSelectedCharacterIndex(before, value)
        }
    var onUpdateCharacters: (List<CharacterModel>) -> Unit = {}

    var onUpdateSelectedCharacterIndex: (before: Int?, after: Int?) -> Unit = { _, _ -> }

    fun updateCharacters(characters: List<CharacterModel>) {
        this.characters = ArrayList(characters)
        selectedCharacterIndex = if (characters.isEmpty()) null else 0
        onUpdateCharacters(characters)
    }
}
