package com.mgtriffid.games.panna.screens.menu.components.characterlist

class CharacterListModel {
    var characters = emptyList<CharacterModel>()

    var selectedCharacterIndex: Int? = 0
        set(value) {
            field = value
            onUpdateSelectedCharacterIndex()
        }
    var onUpdateCharacters: (List<CharacterModel>) -> Unit = {}

    var onUpdateSelectedCharacterIndex: () -> Unit = {}

    fun updateCharacters(characters: List<CharacterModel>) {
        this.characters = ArrayList(characters)
        onUpdateCharacters(characters)
        selectedCharacterIndex = if (characters.isEmpty()) null else 1
    }
}
