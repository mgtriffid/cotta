package com.mgtriffid.games.panna.shared.game.components

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.id.EntityId

interface BulletComponent: Component<BulletComponent> {
    @ComponentData val shooterId: EntityId

    companion object {
        fun create(shooterId: EntityId): BulletComponent = BulletComponentImpl(shooterId)
    }

    override fun copy(): BulletComponent = this
}

private data class BulletComponentImpl(
    override val shooterId: EntityId
) : BulletComponent
