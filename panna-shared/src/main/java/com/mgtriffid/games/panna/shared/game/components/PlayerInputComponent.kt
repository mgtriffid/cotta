package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.annotations.Input
import com.mgtriffid.games.panna.shared.game.MovementDirection

@Input
data class PlayerInputComponent(
    val direction: MovementDirection,
    val jump: Boolean,
    val swingSword: Boolean
)
