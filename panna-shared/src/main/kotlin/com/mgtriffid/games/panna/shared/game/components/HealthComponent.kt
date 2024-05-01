package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.annotations.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

@com.mgtriffid.games.cotta.core.annotations.Component
interface HealthComponent : MutableComponent<HealthComponent> {

    @ComponentData
    var health: Int
    @ComponentData
    val max: Int
}
