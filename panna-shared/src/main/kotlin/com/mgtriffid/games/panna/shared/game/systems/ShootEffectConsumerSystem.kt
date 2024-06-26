package com.mgtriffid.games.panna.shared.game.systems

import com.badlogic.gdx.math.Intersector.intersectSegments
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
import com.mgtriffid.games.panna.shared.game.components.SolidTerrainComponent
import com.mgtriffid.games.panna.shared.game.components.WEAPON_PISTOL
import com.mgtriffid.games.panna.shared.game.components.WEAPON_RAILGUN
import com.mgtriffid.games.panna.shared.game.components.WeaponCooldowns
import com.mgtriffid.games.panna.shared.game.components.WeaponEquippedComponent
import com.mgtriffid.games.panna.shared.game.components.createBulletComponent
import com.mgtriffid.games.panna.shared.game.components.createDrawableComponent
import com.mgtriffid.games.panna.shared.game.components.createPositionComponent
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.GravityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createGravityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createVelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.shooting.ShootEffect
import com.mgtriffid.games.panna.shared.game.effects.visual.RailgunVisualEffect
import com.mgtriffid.games.panna.shared.game.effects.shooting.RailgunShotEffect
import com.mgtriffid.games.panna.shared.game.effects.shooting.createRailgunShotEffect
import com.mgtriffid.games.panna.shared.game.effects.shooting.createShootBulletEffect
import com.mgtriffid.games.panna.shared.game.effects.visual.createRailgunVisualEffect
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Predicted
class ShootEffectConsumerSystem : EffectsConsumerSystem<ShootEffect> {
    override val effectType: Class<ShootEffect> = ShootEffect::class.java
    override fun handle(e: ShootEffect, ctx: EffectProcessingContext) {
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
                ctx.fire(createShootBulletEffect(e.shooterId))
                updateCooldown(weaponEquippedComponent, ctx, WeaponCooldowns.PISTOL)
            }

            WEAPON_RAILGUN -> {
                shootRailgun(shooter, ctx)
                updateCooldown(weaponEquippedComponent, ctx, WeaponCooldowns.RAILGUN)
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
        logger.debug {
            """After cooldown update:
            |weaponEquippedComponent.cooldownUntil = ${weaponEquippedComponent.cooldownUntil}
            |time = $time
        """.trimMargin()
        }
    }

    private fun shootRailgun(shooter: Entity, ctx: EffectProcessingContext) {
        logger.debug { "Shooting railgun" }
        val position = shooter.getComponent(PositionComponent::class)
        val direction = shooter.getComponent(LookingAtComponent::class)
        val railStart = Vector2(position.xPos, position.yPos)
        val railEnd = Vector2(220f, 0f).rotateDeg(direction.lookAt).add(railStart)
        val terrainEntities = ctx.entities().all().filter { it.hasComponent(SolidTerrainComponent::class) }
        val intersectionPoints = terrainEntities.flatMap { terrain ->
            val tCollider = terrain.getComponent(ColliderComponent::class)
            val tPosition = terrain.getComponent(PositionComponent::class)

            /**
             * C1 +-----+ C2
             *    |     |
             *    |     |
             *    |     |
             * C4 +-----+ C3
             */
            val c1 = Vector2(tPosition.xPos - tCollider.width / 2, tPosition.yPos + tCollider.height / 2)
            val c2 = Vector2(tPosition.xPos + tCollider.width / 2, tPosition.yPos + tCollider.height / 2)
            val c3 = Vector2(tPosition.xPos + tCollider.width / 2, tPosition.yPos - tCollider.height / 2)
            val c4 = Vector2(tPosition.xPos - tCollider.width / 2, tPosition.yPos - tCollider.height / 2)
            val intersection1 = lineIntersection(railStart, railEnd, c1, c2)
            val intersection2 = lineIntersection(railStart, railEnd, c2, c3)
            val intersection3 = lineIntersection(railStart, railEnd, c3, c4)
            val intersection4 = lineIntersection(railStart, railEnd, c4, c1)
            listOfNotNull(intersection1, intersection2, intersection3, intersection4)
        }
        val intersectionPoint = if (intersectionPoints.isNotEmpty()) {
            intersectionPoints.minByOrNull { it.cpy().sub(railStart).len() }
        } else null
        ctx.fire(
            createRailgunVisualEffect(
                railStart.x,
                railStart.y,
                (intersectionPoint ?: railEnd).x,
                (intersectionPoint ?: railEnd).y
            )
        )
        ctx.fire(
            createRailgunShotEffect(
                shooter.id,
                (shooter.ownedBy as? Entity.OwnedBy.Player)?.playerId ?: throw IllegalStateException("Who shot?"),
                railStart.x,
                railStart.y,
                (intersectionPoint ?: railEnd).x,
                (intersectionPoint ?: railEnd).y
            )
        )
    }

    private fun lineIntersection(p1: Vector2, p2: Vector2, p3: Vector2, p4: Vector2): Vector2? {
        val intersection = Vector2()
        return if (intersectSegments(p1, p2, p3, p4, intersection)) {
            intersection
        } else {
            null
        }
    }
}
