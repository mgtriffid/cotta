package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.core.entities.Component

interface SolidTerrainComponent : Component<SolidTerrainComponent> {
    companion object {
        object INSTANCE : SolidTerrainComponent
        fun create(): SolidTerrainComponent {
            return INSTANCE
        }
    }

    override fun copy(): SolidTerrainComponent {
        return INSTANCE
    }
}
