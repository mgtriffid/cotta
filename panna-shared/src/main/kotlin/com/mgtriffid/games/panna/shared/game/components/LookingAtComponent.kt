package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface LookingAtComponent : MutableComponent<LookingAtComponent> {
    @ComponentData
    var lookAt: Float

    companion object {
        fun create(lookAt: Float): LookingAtComponent {
            return LookingAtComponentImpl(lookAt)
        }
    }
}

private data class LookingAtComponentImpl(
    override var lookAt: Float
) : LookingAtComponent {
    override fun copy(): LookingAtComponent = this.copy(lookAt = lookAt)
}
