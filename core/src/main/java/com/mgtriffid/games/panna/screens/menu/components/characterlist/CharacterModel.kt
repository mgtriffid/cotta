package com.mgtriffid.games.panna.screens.menu.components.characterlist

import com.badlogic.gdx.graphics.Color

data class CharacterModel(
    val name: String, val color: CharacterColor
) {
    data class CharacterColor(
        val r: Int,
        val g: Int,
        val b: Int
    )
}
