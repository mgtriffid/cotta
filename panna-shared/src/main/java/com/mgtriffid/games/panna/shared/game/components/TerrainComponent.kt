package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component

const val TERRAIN_TYPE_GROUND = 0
const val TERRAIN_TYPE_SAND = 1

interface TerrainComponent: Component<TerrainComponent> {
    companion object {
        fun create(type: Int): TerrainComponent {
            return TerrainComponentImpl(type)
        }
    }

    override fun copy(): TerrainComponent { return this }

    @ComponentData
    val type: Int
}

class TerrainComponentImpl(override val type: Int) : TerrainComponent
