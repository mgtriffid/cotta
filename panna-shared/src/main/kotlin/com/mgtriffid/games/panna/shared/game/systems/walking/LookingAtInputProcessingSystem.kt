package com.mgtriffid.games.panna.shared.game.systems.walking

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.LookingAtComponent
import com.mgtriffid.games.panna.shared.game.components.input.CharacterInputComponent

@Predicted
class LookingAtInputProcessingSystem : InputProcessingSystem {
    override fun process(e: Entity, ctx: InputProcessingContext) {
        if (e.hasComponent(LookingAtComponent::class) && e.hasInputComponent(CharacterInputComponent::class)) {
            val input = e.getInputComponent(CharacterInputComponent::class)
            e.getComponent(LookingAtComponent::class).lookAt = input.lookAt
        }
    }
}