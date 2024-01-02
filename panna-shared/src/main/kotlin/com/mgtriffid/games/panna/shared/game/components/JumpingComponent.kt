package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.MutableComponent

interface JumpingComponent : MutableComponent<JumpingComponent> {
    @ComponentData
    var inAir: Boolean
    @ComponentData
    val jumpSpeed: Int

    companion object {
        fun create(
            inAir: Boolean,
            jumpSpeed: Int
        ): JumpingComponent {
            return JumpingComponentImpl(inAir, jumpSpeed)
        }
    }
}

private data class JumpingComponentImpl(override var inAir: Boolean, override val jumpSpeed: Int) : JumpingComponent {
    override fun copy(): JumpingComponent {
        return this.copy(inAir = inAir, jumpSpeed = jumpSpeed)
    }
}
