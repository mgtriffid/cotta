package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.DrawableComponent
import com.mgtriffid.games.panna.shared.game.components.PannaTextureIds
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.ShootEffect
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Predicted class ShootEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e !is ShootEffect) {
            return
        }
        val shooter = ctx.entities().get(e.shooterId)
        val position = shooter.getComponent(PositionComponent::class)
        val bullet = ctx.createEntity(ownedBy = shooter.ownedBy)
        logger.info { "Created bullet ${bullet.id}" }
        bullet.addComponent(PositionComponent.create(position.xPos, position.yPos, position.orientation))
        bullet.addComponent(DrawableComponent.create(PannaTextureIds.TEXTURE_ID_BULLET))
        bullet.addComponent(
            VelocityComponent.create(when (position.orientation) {
            PositionComponent.ORIENTATION_LEFT -> -80
            PositionComponent.ORIENTATION_RIGHT -> 80
            else -> throw IllegalStateException("Invalid orientation: ${position.orientation}")
        }, 0))
    }
}