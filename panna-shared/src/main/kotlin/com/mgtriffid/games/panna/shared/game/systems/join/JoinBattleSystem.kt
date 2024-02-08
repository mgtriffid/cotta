package com.mgtriffid.games.panna.shared.game.systems.join

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.InputProcessingContext
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent
import com.mgtriffid.games.panna.shared.game.effects.join.JoinBattleEffect
import com.mgtriffid.games.panna.shared.game.effects.join.createJoinBattleEffect

@Predicted
class JoinBattleSystem : InputProcessingSystem {
    override fun process(e: Entity, ctx: InputProcessingContext) {
        if (e.hasInputComponent(JoinBattleMetaEntityInputComponent::class)) {
            val join = e.getInputComponent(JoinBattleMetaEntityInputComponent::class).join
            if (join) {
                if (ctx.entities().all().none {
                    it.hasComponent(SteamManPlayerComponent::class) &&
                        it.ownedBy == e.ownedBy
                    }) {
                    ctx.fire(createJoinBattleEffect(e.id))
                }
            }
        }
    }
}
