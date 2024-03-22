package com.mgtriffid.games.panna.shared.game.systems.shooting

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.BulletComponent
import com.mgtriffid.games.panna.shared.game.components.HealthComponent
import com.mgtriffid.games.panna.shared.game.components.SolidTerrainComponent
import com.mgtriffid.games.panna.shared.game.effects.CollisionEffect
import com.mgtriffid.games.panna.shared.game.effects.shooting.BulletHitsDudeEffect
import com.mgtriffid.games.panna.shared.game.effects.shooting.createBulletHitsDudeEffect
import com.mgtriffid.games.panna.shared.game.effects.visual.createBulletHitsDudeVisualEffect
import com.mgtriffid.games.panna.shared.game.effects.visual.createBulletHitsGroundVisualEffect

@Predicted
class BulletCollisionSystem : EffectsConsumerSystem<CollisionEffect> {
    override val effectType: Class<CollisionEffect> = CollisionEffect::class.java
    override fun handle(e: CollisionEffect, ctx: EffectProcessingContext) {
        val entities = ctx.entities()
        val entity = entities.get(e.id) ?: return
        if (entity.hasComponent(BulletComponent::class)) {
            val collidedWith = entities.get(e.id2) ?: return
            when {
                collidedWith.hasComponent(HealthComponent::class) -> {
                    val bulletComponent = entity.getComponent(BulletComponent::class)
                    if (e.id2 == bulletComponent.shooterId) return

                    ctx.fire(createBulletHitsDudeVisualEffect(e.x, e.y))
                    ctx.fire(createBulletHitsDudeEffect(e.id2))
                    entities.remove(e.id)
                }

                collidedWith.hasComponent(SolidTerrainComponent::class) -> {
                    ctx.fire(createBulletHitsGroundVisualEffect(e.x, e.y))
                    // TODO remove on the next tick? Now it looks like bullet never hits
                    //  the ground. But okay we will see after we increase tick rate to 60. Not Cotta's problem.
                    entities.remove(e.id)
                }
            }
        }
    }
}
