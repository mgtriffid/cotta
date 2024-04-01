package com.mgtriffid.games.panna.shared.game.systems.shooting

import com.badlogic.gdx.math.Intersector.intersectSegmentRectangle
import com.badlogic.gdx.math.Rectangle
import com.mgtriffid.games.cotta.core.annotations.LagCompensated
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.cotta.core.systems.LagCompensatedEffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent
import com.mgtriffid.games.panna.shared.game.effects.shooting.RailgunHitsDudeEffect
import com.mgtriffid.games.panna.shared.game.effects.shooting.RailgunShotEffect
import com.mgtriffid.games.panna.shared.game.effects.shooting.createRailgunHitsDudeEffect

@LagCompensated
class RailgunShotEffectConsumerSystem : LagCompensatedEffectsConsumerSystem<RailgunShotEffect> {
    override fun player(effect: RailgunShotEffect): PlayerId {
        return effect.shooterPlayerId
    }

    override val effectType: Class<RailgunShotEffect> = RailgunShotEffect::class.java
    override fun handle(e: RailgunShotEffect, ctx: EffectProcessingContext) {

        ctx.entities().all().filter {
            // "Damageable" would be a bit more clean
            it.hasComponent(SteamManPlayerComponent::class)
        }.filter {
            it.id != e.shooterId
        }.filter {
            it.isHitBy(e)
        }.forEach { ctx.fire(createRailgunHitsDudeEffect(it.id)) }
    }

    private fun Entity.isHitBy(e: RailgunShotEffect): Boolean {
        val position = getComponent(PositionComponent::class)
        val collider = getComponent(ColliderComponent::class)
        val rectangle = Rectangle(
            position.xPos - collider.width / 2,
            position.yPos - collider.height / 2,
            collider.width.toFloat(),
            collider.height.toFloat()
        )
        return intersectSegmentRectangle(
            e.x1, e.y1, e.x2, e.y2, rectangle
        ).also {
            if (it) {
                println("Here we debug")
            }
        }
    }
}
