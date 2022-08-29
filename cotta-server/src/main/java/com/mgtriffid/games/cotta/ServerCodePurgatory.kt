package com.mgtriffid.games.cotta

import com.mgtriffid.games.cotta.core.CottaGame

/**
 * Temporary class that hosts code which is yet to be settled somewhere. Let's call this "nowhere" and
 * put some code here, and then we'll sort it out later where to put it. For now we don't know what belongs
 * to server, what belongs to game class, network manager, etc etc., what belongs to Cotta framework, what
 * belongs to Panna game.
 */
class ServerCodePurgatory(
    val game: CottaGame
) {
    lateinit var state: Any

    fun getNonPlayerInput(): Any {
        return game.calculateNonPlayerInput(state)
    }
}
