package com.mgtriffid.games.panna.shared.game.systems.join

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.CHARACTER_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.*
import com.mgtriffid.games.panna.shared.game.components.input.CharacterInputComponent
import com.mgtriffid.games.panna.shared.game.components.input.ShootInputComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createGravityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createVelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.join.JoinBattleEffect

@Predicted
class JoinBattleEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is JoinBattleEffect) {
            val dude = ctx.createEntity(ctx.entities().getOrNotFound(e.metaEntityId).ownedBy)
            dude.addComponent(createPositionComponent(32f, 24f))
            dude.addInputComponent(CharacterInputComponent::class)
            dude.addInputComponent(ShootInputComponent::class)
            dude.addComponent(createJumpingComponent(false, 250f))
            dude.addComponent(createWalkingComponent(100f))
            dude.addComponent(createGravityComponent())
            dude.addComponent(createLookingAtComponent(0f))
            dude.addComponent(createHealthComponent(77, 100))
            dude.addComponent(createVelocityComponent(0f, 0f))
            dude.addComponent(createSteamManPlayerComponent())
            dude.addComponent(createColliderComponent(16, 16))
            dude.addComponent(createDrawableComponent(CHARACTER_STRATEGY))
            dude.addComponent(createWeaponEquippedComponent(WEAPON_PISTOL, 0, 0))
        }
    }
}
