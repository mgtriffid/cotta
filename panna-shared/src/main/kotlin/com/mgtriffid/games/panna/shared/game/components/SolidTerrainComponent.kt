package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

interface SolidTerrainComponent : Component<SolidTerrainComponent> {
    @ComponentData val width: Int
    @ComponentData val height: Int
    companion object {
        fun create(width: Int, height: Int): SolidTerrainComponent {
            return SolidTerrainComponentImpl(width, height)
        }
    }
}

data class SolidTerrainComponentImpl(override val width: Int, override val height: Int): SolidTerrainComponent {
    override fun copy(): SolidTerrainComponent {
        return this.copy(width = width, height = height)
    }
}
