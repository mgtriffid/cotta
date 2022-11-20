package com.mgtriffid.games.panna.screens.menu.components.characterlist

data class CharacterModel(
    val name: String, val color: CharacterColor
) {
    data class CharacterColor(
        val r: Int,
        val g: Int,
        val b: Int
    )
}
