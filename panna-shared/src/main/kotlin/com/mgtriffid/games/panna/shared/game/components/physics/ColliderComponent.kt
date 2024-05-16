package com.mgtriffid.games.panna.shared.game.components.physics

import com.mgtriffid.games.cotta.core.annotations.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

@com.mgtriffid.games.cotta.core.annotations.Component
interface ColliderComponent : Component {
    @ComponentData
    val width: Int
    @ComponentData
    val height: Int
}
