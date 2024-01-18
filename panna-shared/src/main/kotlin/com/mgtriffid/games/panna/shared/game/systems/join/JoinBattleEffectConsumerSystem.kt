package com.mgtriffid.games.panna.shared.game.systems.join

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.CHARACTER_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.*
import com.mgtriffid.games.panna.shared.game.components.PositionComponent.Companion.ORIENTATION_LEFT
import com.mgtriffid.games.panna.shared.game.components.input.ShootInputComponent
import com.mgtriffid.games.panna.shared.game.components.input.CharacterInputComponent
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.GravityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.join.JoinBattleEffect

@Predicted
class JoinBattleEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is JoinBattleEffect) {
            val dude = ctx.createEntity(ownedBy = e.ownedBy)
            dude.addComponent(PositionComponent.create(32f, 24f, ORIENTATION_LEFT))
            dude.addInputComponent(CharacterInputComponent::class)
            dude.addInputComponent(ShootInputComponent::class)
            dude.addComponent(JumpingComponent.create(false, 250f))
            dude.addComponent(WalkingComponent.create(100f))
            dude.addComponent(GravityComponent.Instance)
            dude.addComponent(LookingAtComponent.create(0f))
            dude.addComponent(VelocityComponent.create(0f, 0f))
            dude.addComponent(SteamManPlayerComponent.create())
            dude.addComponent(ColliderComponent.create(16, 16))
            dude.addComponent(DrawableComponent.create(CHARACTER_STRATEGY))
        }
    }
}
