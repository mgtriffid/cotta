package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface HealthComponent : MutableComponent<HealthComponent> {

    @ComponentData
    var health: Int
    @ComponentData val max: Int

    companion object {
        fun create(health: Int, max: Int): HealthComponent {
            return HealthComponentImpl(health, max)
        }
    }
}

private data class HealthComponentImpl(
    override var health: Int,
    override val max: Int
) : HealthComponent {
    override fun copy(): HealthComponent = this.copy(health = health)
}
