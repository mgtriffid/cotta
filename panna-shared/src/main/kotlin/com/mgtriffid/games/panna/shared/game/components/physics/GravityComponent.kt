package com.mgtriffid.games.panna.shared.game.components.physics

import com.mgtriffid.games.cotta.core.entities.Component

interface GravityComponent : Component<GravityComponent> {
    object Instance: GravityComponent

    companion object {
        fun create(): GravityComponent {
            return Instance
        }
    }

    override fun copy() = this
}
