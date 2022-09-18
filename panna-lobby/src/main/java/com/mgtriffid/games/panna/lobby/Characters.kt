package com.mgtriffid.games.panna.lobby


class Characters {
    private val characters = listOf(
        PannaCharacter(
            name = CharacterName("Jozella"),
            color = CharacterColor(52, 231, 30),
            player = Username("Abel")
        ),
        PannaCharacter(
            name = CharacterName("Bill"),
            color = CharacterColor(1, 213, 250),
            player = Username("Abel")
        ),
        PannaCharacter(
            name = CharacterName("Justin"),
            color = CharacterColor(245, 97, 7),
            player = Username("Baker")
        ),
        PannaCharacter(
            name = CharacterName("Ebosher"),
            color = CharacterColor(140, 71, 182),
            player = Username("Baker")
        ),
        PannaCharacter(
            name = CharacterName("Unkind"),
            color = CharacterColor(141, 133, 112),
            player = Username("Baker")
        ),
        PannaCharacter(
            name = CharacterName("Homa"),
            color = CharacterColor(35, 194, 167),
            player = Username("Charlie")
        ),
    )

    fun forUser(username: Username) = characters.filter { it.player == username }
}

data class PannaCharacter(
    val name: CharacterName,
    val color: CharacterColor,
    val player: Username // TODO playerId once introduced
)

data class CharacterName(val name: String)

data class CharacterColor(
    val r: Int,
    val g: Int,
    val b: Int
) {
    init {
        listOf(r, g, b).forEach {
            if (it < 0 || it > 255) {
                throw IllegalArgumentException("Value $it is outside of [0, 255] interval")
            }
        }
    }
}
