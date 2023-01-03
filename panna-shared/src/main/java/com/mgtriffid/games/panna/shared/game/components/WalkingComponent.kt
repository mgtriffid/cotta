package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.panna.shared.game.MovementDirection

data class WalkingComponent(
    var movementDirection: MovementDirection
): Component
