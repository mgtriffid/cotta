package com.mgtriffid.games.cotta.core.input.impl

import com.mgtriffid.games.cotta.core.input.GameInput
import com.mgtriffid.games.cotta.core.input.NonPlayersInput
import com.mgtriffid.games.cotta.core.input.PlayersInput

class GameInputImpl(
    val playersInput: PlayersInput,
    val nonPlayersInput: NonPlayersInput
) : GameInput {
}
