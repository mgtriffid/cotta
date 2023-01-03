package com.mgtriffid.games.cotta.core.entities.workload.components

import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface PositionTestComponent : MutableComponent<PositionTestComponent> {
    companion object {
        fun create(x: Int, y: Int): PositionTestComponent {
            return PositionTestComponentImpl(x, y)
        }
    }

    var x: Int
    var y: Int

    override fun copy(): PositionTestComponent {
        return PositionTestComponentImpl(x, y)
    }
}

private data class PositionTestComponentImpl(
    override var x: Int,
    override var y: Int
): PositionTestComponent
