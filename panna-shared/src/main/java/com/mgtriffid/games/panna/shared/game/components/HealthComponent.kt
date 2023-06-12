package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface HealthComponent : MutableComponent<HealthComponent> {
    @ComponentData var health: Int
    @ComponentData val maxHealth: Int

    companion object {
        fun create(health: Int, maxHealth: Int): HealthComponent {
            return HealthComponentImpl(health, maxHealth)
        }
    }

    override fun copy(): HealthComponent {
        TODO("Not yet implemented")
    }
}

data class HealthComponentImpl(override var health: Int, override val maxHealth: Int) : HealthComponent

