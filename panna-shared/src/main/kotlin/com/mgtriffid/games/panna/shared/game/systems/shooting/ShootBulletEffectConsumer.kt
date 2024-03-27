package com.mgtriffid.games.panna.shared.game.systems.shooting

import com.badlogic.gdx.math.Vector2
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.BULLET_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.LookingAtComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.createBulletComponent
import com.mgtriffid.games.panna.shared.game.components.createDrawableComponent
import com.mgtriffid.games.panna.shared.game.components.createPositionComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createGravityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.createVelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.shooting.ShootBulletEffect
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ShootBulletEffectConsumer : EffectsConsumerSystem<ShootBulletEffect> {
    override val effectType: Class<ShootBulletEffect> = ShootBulletEffect::class.java

    override fun handle(e: ShootBulletEffect, ctx: EffectProcessingContext) {
        val shooter = ctx.entities().getOrNotFound(e.shooterId)
        logger.debug { "Shooting bullet" }
        val position = shooter.getComponent(PositionComponent::class)
        val direction = shooter.getComponent(LookingAtComponent::class)
        val bullet = ctx.createEntity(ownedBy = shooter.ownedBy)
        val velocity = (Vector2(530f, 0f)).rotateDeg(direction.lookAt)
        logger.debug { "Created bullet ${bullet.id}" }
        bullet.addComponent(createPositionComponent(position.xPos, position.yPos))
        bullet.addComponent(createDrawableComponent(BULLET_STRATEGY))
        bullet.addComponent(createColliderComponent(2, 2))
        bullet.addComponent(createGravityComponent())
        bullet.addComponent(createBulletComponent(shooter.id))
        bullet.addComponent(
            createVelocityComponent(velocity.x, velocity.y)
        )
    }
}
