package com.mgtriffid.games.panna.shared.game.systems.join

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.CHARACTER_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.WALKING_DIRECTION_NONE
import com.mgtriffid.games.panna.shared.game.components.WEAPON_PISTOL
import com.mgtriffid.games.panna.shared.game.components.createCharacterInputComponent2
import com.mgtriffid.games.panna.shared.game.components.createDrawableComponent
import com.mgtriffid.games.panna.shared.game.components.createHealthComponent
import com.mgtriffid.games.panna.shared.game.components.createJumpingComponent
import com.mgtriffid.games.panna.shared.game.components.createLookingAtComponent
import com.mgtriffid.games.panna.shared.game.components.createPositionComponent
import com.mgtriffid.games.panna.shared.game.components.createShootComponent
import com.mgtriffid.games.panna.shared.game.components.createSteamManPlayerComponent
import com.mgtriffid.games.panna.shared.game.components.createWalkingComponent
import com.mgtriffid.games.panna.shared.game.components.createWeaponEquippedComponent
import com.mgtriffid.games.panna.shared.game.components.input.ShootInputComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createGravityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createVelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.join.JoinBattleEffect

@Predicted
class JoinBattleEffectConsumerSystem : EffectsConsumerSystem<JoinBattleEffect> {
    override val effectType: Class<JoinBattleEffect> = JoinBattleEffect::class.java
    override fun handle(e: JoinBattleEffect, ctx: EffectProcessingContext) {
        val dude = ctx.createEntity(Entity.OwnedBy.Player(e.playerId))
        dude.addComponent(createPositionComponent(32f, 24f))
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
        dude.addComponent(
            createWeaponEquippedComponent(
                WEAPON_PISTOL,
                0,
                0
            )
        )
        dude.addComponent(createShootComponent(false))
        dude.addComponent(
            createCharacterInputComponent2(
                WALKING_DIRECTION_NONE,
                false,
                0f,
                0
            )
        )
    }
}
