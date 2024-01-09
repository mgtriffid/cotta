package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

interface WalkingComponent: Component<WalkingComponent> {
    @ComponentData
    val speed: Float

    companion object {
        fun create(speed: Float): WalkingComponent {
            return WalkingComponentImpl(speed)
        }
    }

    override fun copy(): WalkingComponent {
        return WalkingComponentImpl(speed)
    }
}

private data class WalkingComponentImpl(override val speed: Float): WalkingComponent
