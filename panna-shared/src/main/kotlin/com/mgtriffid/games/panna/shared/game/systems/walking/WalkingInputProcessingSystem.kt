package com.mgtriffid.games.panna.shared.game.systems.walking

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.CharacterInputComponent2
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import com.mgtriffid.games.panna.shared.game.effects.walking.createJumpEffect
import com.mgtriffid.games.panna.shared.game.effects.walking.createWalkingEffect

@Predicted
class WalkingInputProcessingSystem : EntityProcessingSystem {
    override fun process(e: Entity, ctx: EntityProcessingContext) {
        if (e.hasComponent(CharacterInputComponent2::class) && e.hasComponent(WalkingComponent::class)) {
            val input = e.getComponent(CharacterInputComponent2::class)
            ctx.fire(createWalkingEffect(e.id, input.direction))
            if (input.jump && e.hasComponent(JumpingComponent::class) && !e.getComponent(JumpingComponent::class).inAir) {
                ctx.fire(createJumpEffect(e.id))
            }
        }
    }
}
