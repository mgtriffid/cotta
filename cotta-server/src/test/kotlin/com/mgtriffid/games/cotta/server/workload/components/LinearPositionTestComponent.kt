package com.mgtriffid.games.cotta.server.workload.components

import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface LinearPositionTestComponent: MutableComponent<LinearPositionTestComponent> {
    companion object {
        fun create(x: Int): LinearPositionTestComponent {
            return LinearPositionTestComponentImpl(x)
        }
    }

    var x: Int

    override fun copy(): LinearPositionTestComponent {
        return LinearPositionTestComponentImpl(x)
    }
}

private data class LinearPositionTestComponentImpl(
    override var x: Int,
) : LinearPositionTestComponent
