package com.mgtriffid.games.panna.shared.game.systems

import com.badlogic.gdx.math.Vector2
import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.BULLET_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.BulletComponent
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.LookingAtComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.WEAPON_PISTOL
import com.mgtriffid.games.panna.shared.game.components.WEAPON_RAILGUN
import com.mgtriffid.games.panna.shared.game.components.WeaponCooldowns
import com.mgtriffid.games.panna.shared.game.components.WeaponEquippedComponent
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.GravityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.shooting.ShootEffect
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Predicted
class ShootEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e !is ShootEffect) {
            return
        }
        val shooter = ctx.entities().get(e.shooterId) ?: return
        val weaponEquippedComponent = shooter.getComponent(WeaponEquippedComponent::class)
        logger.debug { "ctx.clock().time = ${ctx.clock().time()}" }
        logger.debug { "weaponEquippedComponent.cooldownUntil = ${weaponEquippedComponent.cooldownUntil}" }
        if (ctx.clock().time() < weaponEquippedComponent.cooldownUntil) {
            logger.debug { "Not shooting" }
            return
        }
        when (weaponEquippedComponent.equipped) {
            WEAPON_PISTOL -> {
                shootBullet(shooter, ctx)
                updateCooldown(weaponEquippedComponent, ctx, WeaponCooldowns.PISTOL)
            }
            WEAPON_RAILGUN -> {
                logger.debug { "Shooting railgun" }
            }
            else -> {
                logger.debug { "Shooting nothing" }
                return
            }
        }
    }

    private fun updateCooldown(
        weaponEquippedComponent: WeaponEquippedComponent,
        ctx: EffectProcessingContext,
        cooldown: Long
    ) {
        val time = ctx.clock().time()
        if (weaponEquippedComponent.cooldownUntil < time - 1000f * ctx.clock().delta()) {
            weaponEquippedComponent.cooldownUntil = time + cooldown
        } else {
            weaponEquippedComponent.cooldownUntil += cooldown
        }
        logger.debug { """After cooldown update:
            |weaponEquippedComponent.cooldownUntil = ${weaponEquippedComponent.cooldownUntil}
            |time = $time
        """.trimMargin() }
    }

    private fun shootBullet(shooter: Entity, ctx: EffectProcessingContext) {
        logger.debug { "Shooting bullet" }
        val position = shooter.getComponent(PositionComponent::class)
        val direction = shooter.getComponent(LookingAtComponent::class)
        val bullet = ctx.createEntity(ownedBy = shooter.ownedBy)
        val velocity = (Vector2(530f, 0f)).rotateDeg(direction.lookAt)
        logger.debug { "Created bullet ${bullet.id}" }
        bullet.addComponent(PositionComponent.create(position.xPos, position.yPos))
        bullet.addComponent(DrawableComponent.create(BULLET_STRATEGY))
        bullet.addComponent(ColliderComponent.create(2, 2))
        bullet.addComponent(GravityComponent.create())
        bullet.addComponent(BulletComponent.create(shooter.id))
        bullet.addComponent(
            VelocityComponent.create(velocity.x, velocity.y)
        )
    }
}
