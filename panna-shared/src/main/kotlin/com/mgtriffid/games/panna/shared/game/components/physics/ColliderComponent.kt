package com.mgtriffid.games.panna.shared.game.components.physics

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

@com.mgtriffid.games.cotta.Component
interface ColliderComponent : Component<ColliderComponent> {
    @ComponentData val width: Int
    @ComponentData val height: Int
}
