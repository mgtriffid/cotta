package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface HealthTestComponent : MutableComponent<HealthTestComponent> {
    companion object {
        fun create(health: Int): HealthTestComponent {
            return HealthTestComponentImpl(health)
        }
    }

    var health: Int

    override fun copy(): HealthTestComponent {
        return HealthTestComponentImpl(health)
    }
}

private data class HealthTestComponentImpl(
    override var health: Int,
): HealthTestComponent
