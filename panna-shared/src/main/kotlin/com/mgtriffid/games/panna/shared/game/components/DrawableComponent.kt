package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

@com.mgtriffid.games.cotta.Component
interface DrawableComponent : Component<DrawableComponent> {
    @ComponentData val drawStrategy: Int
}
