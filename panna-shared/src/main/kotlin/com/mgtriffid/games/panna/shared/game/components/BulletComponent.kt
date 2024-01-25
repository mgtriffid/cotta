package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.entities.Component

interface BulletComponent: Component<BulletComponent> {
    companion object {
        private val INSTANCE = object : BulletComponent {
            override fun copy(): BulletComponent = this
        }
        fun create(): BulletComponent = INSTANCE
    }
}
