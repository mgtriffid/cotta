package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.entities.Component

interface VelocityTestComponent : Component<VelocityTestComponent> {
    companion object {
        fun create(velocity: Int): VelocityTestComponent = VelocityTestComponentImpl(velocity)
    }

    override fun copy() = create(velocity)

    val velocity: Int
}

private data class VelocityTestComponentImpl(
    override val velocity: Int
): VelocityTestComponent
