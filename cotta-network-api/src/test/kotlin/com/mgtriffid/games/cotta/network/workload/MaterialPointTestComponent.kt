package com.mgtriffid.games.cotta.network.workload

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface MaterialPointTestComponent : MutableComponent<MaterialPointTestComponent> {
    companion object {
        fun create(mass: Int, xPos: Int, yPos: Int): MaterialPointTestComponent {
            return MaterialPointTestComponentImpl(mass, xPos, yPos)
        }
    }
    @ComponentData val mass: Int
    @ComponentData var xPos: Int
    @ComponentData var yPos: Int

    override fun copy(): MaterialPointTestComponent {
        return MaterialPointTestComponentImpl(mass, xPos, yPos)
    }
}

private data class MaterialPointTestComponentImpl(
    override val mass: Int,
    override var xPos: Int,
    override var yPos: Int
): MaterialPointTestComponent
