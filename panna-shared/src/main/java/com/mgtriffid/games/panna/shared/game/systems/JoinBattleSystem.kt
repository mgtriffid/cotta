package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent
import com.mgtriffid.games.panna.shared.game.effects.JoinBattleEffect
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Predicted
class JoinBattleSystem : InputProcessingSystem {
    override fun process(e: Entity, ctx: InputProcessingContext) {
        if (e.hasInputComponent(JoinBattleMetaEntityInputComponent::class)) {
            val join = e.getInputComponent(JoinBattleMetaEntityInputComponent::class).join
            if (join) {
                // todo make sure it doesn't fire twice
                // when this fires then we record some context
                logger.info { "Firing JoinBattleEffect" }
                ctx.fire(JoinBattleEffect(e.ownedBy))
            }
        }
    }
}
