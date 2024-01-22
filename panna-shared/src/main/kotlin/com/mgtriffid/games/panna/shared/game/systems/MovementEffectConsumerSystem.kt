package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent.Companion.ORIENTATION_LEFT
import com.mgtriffid.games.panna.shared.game.components.PositionComponent.Companion.ORIENTATION_RIGHT
import com.mgtriffid.games.panna.shared.game.components.SolidTerrainComponent
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect
import com.mgtriffid.games.panna.shared.utils.intersect
import kotlin.math.max
import kotlin.math.min

private val logger = mu.KotlinLogging.logger {}

@Predicted
class MovementEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is MovementEffect) {
            logger.trace { "Received MovementEffect: $e" }
            val entity = ctx.entities().get(e.entityId)
            val velocityComponent = entity.getComponent(VelocityComponent::class)
            val land: () -> Unit = if (entity.hasComponent(JumpingComponent::class)) {
                {
                    entity.getComponent(JumpingComponent::class).inAir = false
                    logger.trace { "Landed!" }
                }
            } else { {
                logger.trace { "Landed! but who cares" }
            } }
            if (entity.hasComponent(PositionComponent::class)) {
                val position = entity.getComponent(PositionComponent::class)
                var newXPos = position.xPos + e.velocityX
                var newYPos = position.yPos + e.velocityY

                when {
                    e.velocityX < 0 -> position.orientation = ORIENTATION_LEFT
                    e.velocityX > 0 -> position.orientation = ORIENTATION_RIGHT
                }

                // TODO indexed query
                if (entity.hasComponent(ColliderComponent::class)) {
                    val collider = entity.getComponent(ColliderComponent::class)
                    val rectLeft = min(position.xPos, newXPos) - collider.width / 2
                    val rectRight = max(position.xPos, newXPos) + collider.width / 2
                    val rectTop = max(position.yPos, newYPos) + collider.height / 2
                    val rectBottom = min(position.yPos, newYPos) - collider.height / 2

                    val terrainBlocks = ctx.entities().all()
                        .filter { it.hasComponent(SolidTerrainComponent::class) && it.hasComponent(PositionComponent::class) && it.hasComponent(ColliderComponent::class) }

                    // resolve collisions by X:
                    val xColliding = terrainBlocks.filter {
                        val tPos = it.getComponent(PositionComponent::class)
                        val tCol = it.getComponent(ColliderComponent::class)
                        intersect(
                            l1 = rectLeft,
                            r1 =  rectRight,
                            b1 = position.yPos - collider.height / 2,
                            t1 = position.yPos + collider.height / 2,
                            l2 = tPos.xPos - tCol.width / 2,
                            r2 = tPos.xPos + tCol.width / 2,
                            b2 = tPos.yPos - tCol.height / 2,
                            t2 = tPos.yPos + tCol.height / 2
                        )
                    }
                    when {
                        e.velocityX > 0 -> newXPos = xColliding.fold(newXPos) { acc, t ->
                            val tPos = t.getComponent(PositionComponent::class)
                            val tCol = t.getComponent(ColliderComponent::class)
                            velocityComponent.velX = 0f
                            acc.coerceAtMost(tPos.xPos - tCol.width / 2 - collider.width / 2)
                        }
                        e.velocityX < 0 -> newXPos = xColliding.fold(newXPos) { acc, t ->
                            val tPos = t.getComponent(PositionComponent::class)
                            val tCol = t.getComponent(ColliderComponent::class)
                            velocityComponent.velX = 0f
                            acc.coerceAtLeast(tPos.xPos + tCol.width / 2 + collider.width / 2)
                        }
                    }

                    // resolve collisions by Y:
                    val yColliding = terrainBlocks.filter {
                        val tPos = it.getComponent(PositionComponent::class)
                        val tCol = it.getComponent(ColliderComponent::class)
                        intersect(
                            l1 = newXPos - collider.width / 2,
                            r1 = newXPos + collider.width / 2,
                            b1 = rectBottom,
                            t1 = rectTop,
                            l2 = tPos.xPos - tCol.width / 2,
                            r2 = tPos.xPos + tCol.width / 2,
                            b2 = tPos.yPos - tCol.height / 2,
                            t2 = tPos.yPos + tCol.height / 2
                        )
                    }
                    when {
                        e.velocityY < 0 -> newYPos = yColliding.fold(newYPos) { acc, t ->
                            val tPos = t.getComponent(PositionComponent::class)
                            val tCol = t.getComponent(ColliderComponent::class)
                            velocityComponent.velY = 0f
                            land()
                            acc.coerceAtLeast(tPos.yPos + tCol.height / 2 + collider.height / 2)
                        }
                        e.velocityY > 0 -> newYPos = yColliding.fold(newYPos) { acc, t ->
                            val tPos = t.getComponent(PositionComponent::class)
                            val tCol = t.getComponent(ColliderComponent::class)
                            velocityComponent.velY = 0f
                            acc.coerceAtMost(tPos.yPos - tCol.height / 2 - collider.height / 2)
                        }
                    }
                }

                position.xPos = newXPos
                position.yPos = newYPos

                if (position.xPos < -50 || position.xPos > 1000 || position.yPos < -100 || position.yPos > 2000) {
                    ctx.entities().remove(e.entityId)
                }
            }
        }
    }
}
