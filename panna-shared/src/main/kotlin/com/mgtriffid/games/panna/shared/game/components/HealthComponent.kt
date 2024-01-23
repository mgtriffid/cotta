package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

interface HealthComponent : Component<HealthComponent> {

    @ComponentData
    var health: Int

    companion object {
        fun create(health: Int): HealthComponent {
            return HealthComponentImpl(health)
        }
    }
}

private data class HealthComponentImpl(
    override var health: Int
) : HealthComponent {
    override fun copy(): HealthComponent = this.copy(health = health)
}
