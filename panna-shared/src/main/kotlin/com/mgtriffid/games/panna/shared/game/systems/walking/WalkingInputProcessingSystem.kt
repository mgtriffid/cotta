package com.mgtriffid.games.panna.shared.game.systems.walking

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import com.mgtriffid.games.panna.shared.game.components.input.CharacterInputComponent
import com.mgtriffid.games.panna.shared.game.effects.walking.JumpEffect
import com.mgtriffid.games.panna.shared.game.effects.walking.WalkingEffect

@Predicted
class WalkingInputProcessingSystem : InputProcessingSystem {
    override fun process(e: Entity, ctx: InputProcessingContext) {
        if (e.hasInputComponent(CharacterInputComponent::class) && e.hasComponent(WalkingComponent::class)) {
            val input = e.getInputComponent(CharacterInputComponent::class)
            ctx.fire(WalkingEffect.create(e.id, input.direction))
            if (input.jump && e.hasComponent(JumpingComponent::class) && !e.getComponent(JumpingComponent::class).inAir) {
                ctx.fire(JumpEffect.create(e.id))
            }
        }
    }
}
