package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.annotations.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@com.mgtriffid.games.cotta.core.annotations.Component
interface ShootComponent : MutableComponent<ShootComponent> {
    @ComponentData
    var isShooting: Boolean
}
