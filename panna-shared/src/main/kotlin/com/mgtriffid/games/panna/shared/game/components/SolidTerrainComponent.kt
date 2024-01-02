package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.entities.Component

interface SolidTerrainComponent : Component<SolidTerrainComponent> {
    companion object {
        private val INSTANCE = object : SolidTerrainComponent {
            override fun copy(): SolidTerrainComponent = this
        }
        fun create(): SolidTerrainComponent = INSTANCE
    }
}
