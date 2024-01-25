package com.mgtriffid.games.panna.shared.game.systems

import com.badlogic.gdx.math.Vector2
import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.BULLET_STRATEGY
import com.mgtriffid.games.panna.shared.game.components.BulletComponent
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.LookingAtComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.physics.GravityComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.ShootEffect
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Predicted
class ShootEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e !is ShootEffect) {
            return
        }
        val shooter = ctx.entities().get(e.shooterId)
        val position = shooter.getComponent(PositionComponent::class)
        val direction = shooter.getComponent(LookingAtComponent::class)
        val bullet = ctx.createEntity(ownedBy = shooter.ownedBy)
        val velocity = (Vector2(530f, 0f)).rotateDeg(direction.lookAt)
        logger.debug { "Created bullet ${bullet.id}" }
        bullet.addComponent(PositionComponent.create(position.xPos, position.yPos))
        bullet.addComponent(DrawableComponent.create(BULLET_STRATEGY))
        bullet.addComponent(GravityComponent.create())
        bullet.addComponent(BulletComponent.create())
        bullet.addComponent(
            VelocityComponent.create(velocity.x, velocity.y)
        )
    }
}
