package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent
import com.mgtriffid.games.panna.shared.game.effects.JoinBattleEffect
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class JoinBattleSystem(
    private val entities: Entities,
    private val effectBus: EffectBus
) : InputProcessingSystem {
    override fun process(e: Entity) {
        if (e.hasInputComponent(JoinBattleMetaEntityInputComponent::class)) {
            val join = e.getInputComponent(JoinBattleMetaEntityInputComponent::class).join
            logger.debug { "e.getInputComponent(JoinBattleMetaEntityInputComponent::class).join = $join" }
            if (join) {
                // todo make sure it doesn't fire twice
                effectBus.fire(JoinBattleEffect(e.ownedBy))
            }
        }
    }
}
