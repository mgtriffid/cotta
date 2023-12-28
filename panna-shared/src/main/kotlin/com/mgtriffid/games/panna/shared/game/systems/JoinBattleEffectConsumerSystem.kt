package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.*
import com.mgtriffid.games.panna.shared.game.components.PositionComponent.Companion.ORIENTATION_LEFT
import com.mgtriffid.games.panna.shared.game.components.input.ShootInputComponent
import com.mgtriffid.games.panna.shared.game.components.input.WalkingInputComponent
import com.mgtriffid.games.panna.shared.game.effects.JoinBattleEffect

@Predicted
class JoinBattleEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is JoinBattleEffect) {
            val dude = ctx.createEntity(ownedBy = e.ownedBy)
            dude.addComponent(PositionComponent.create(200, 250, ORIENTATION_LEFT))
            dude.addInputComponent(WalkingInputComponent::class)
            dude.addInputComponent(ShootInputComponent::class)
            dude.addComponent(WalkingComponent.create(15))
            dude.addComponent(SteamManPlayerComponent.create())
            dude.addComponent(DrawableComponent.create(PannaTextureIds.TEXTURE_ID_PLAYER_ENTITY))
        }
    }
}
