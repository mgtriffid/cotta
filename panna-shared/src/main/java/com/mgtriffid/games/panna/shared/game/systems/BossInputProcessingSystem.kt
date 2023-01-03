package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.panna.shared.game.components.BossInputComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent

class BossInputProcessingSystem : CottaSystem {
    override fun update(e: Entity) {
        if (e.hasComponent(BossInputComponent::class) && e.hasComponent(WalkingComponent::class)) {
            val bossInputComponent = e.getComponent(BossInputComponent::class)
            val walkingComponent = e.getComponent(WalkingComponent::class)
            walkingComponent.movementDirection = bossInputComponent.movementDirection
        }
    }
}
