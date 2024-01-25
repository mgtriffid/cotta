package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.annotations.Predicted
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EffectProcessingContext
import com.mgtriffid.games.cotta.core.systems.EffectsConsumerSystem
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.PositionComponent
import com.mgtriffid.games.panna.shared.game.components.SolidTerrainComponent
import com.mgtriffid.games.panna.shared.game.components.WalkingComponent
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.CollisionEffect
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
            if (entity.hasComponent(PositionComponent::class)) {
                val position = entity.getComponent(PositionComponent::class)
                val originalX = position.xPos
                val originalY = position.yPos
                var newXPos = position.xPos + e.velocityX
                var newYPos = position.yPos + e.velocityY

                // TODO indexed query
                if (entity.hasComponent(ColliderComponent::class)) {
                    if (entity.hasComponent(WalkingComponent::class)) {
                        val pair = resolveTerrainCollisions(entity, position, newXPos, newYPos, ctx, e, velocityComponent)

                        newXPos = pair.first
                        newYPos = pair.second
                    }

                    detectCollisions(entity, originalX, originalY, newXPos, newYPos, ctx)
                }

                position.xPos = newXPos
                position.yPos = newYPos

                if (position.xPos < -50 || position.xPos > 1000 || position.yPos < -100 || position.yPos > 2000) {
                    ctx.entities().remove(e.entityId)
                }
            }
        }
    }

    private fun resolveTerrainCollisions(
        entity: Entity,
        position: PositionComponent,
        newXPos: Float,
        newYPos: Float,
        ctx: EffectProcessingContext,
        e: MovementEffect,
        velocityComponent: VelocityComponent,
    ): Pair<Float, Float> {

        val land: () -> Unit = if (entity.hasComponent(JumpingComponent::class)) {
            {
                entity.getComponent(JumpingComponent::class).inAir = false
                logger.trace { "Landed!" }
            }
        } else { { logger.trace { "Landed! but who cares" } } }

        var newXPos1 = newXPos
        var newYPos1 = newYPos
        val collider = entity.getComponent(ColliderComponent::class)
        val rectLeft = min(position.xPos, newXPos1) - collider.width / 2
        val rectRight = max(position.xPos, newXPos1) + collider.width / 2
        val rectTop = max(position.yPos, newYPos1) + collider.height / 2
        val rectBottom = min(position.yPos, newYPos1) - collider.height / 2

        val terrainBlocks = ctx.entities().all()
            .filter {
                it.hasComponent(SolidTerrainComponent::class) && it.hasComponent(PositionComponent::class) && it.hasComponent(
                    ColliderComponent::class
                )
            }

        // resolve collisions by X:
        val xColliding = terrainBlocks.filter {
            val tPos = it.getComponent(PositionComponent::class)
            val tCol = it.getComponent(ColliderComponent::class)
            intersect(
                l1 = rectLeft,
                r1 = rectRight,
                b1 = position.yPos - collider.height / 2,
                t1 = position.yPos + collider.height / 2,
                l2 = tPos.xPos - tCol.width / 2,
                r2 = tPos.xPos + tCol.width / 2,
                b2 = tPos.yPos - tCol.height / 2,
                t2 = tPos.yPos + tCol.height / 2
            )
        }
        when {
            e.velocityX > 0 -> newXPos1 = xColliding.fold(newXPos1) { acc, t ->
                val tPos = t.getComponent(PositionComponent::class)
                val tCol = t.getComponent(ColliderComponent::class)
                velocityComponent.velX = 0f
                acc.coerceAtMost(tPos.xPos - tCol.width / 2 - collider.width / 2)
            }

            e.velocityX < 0 -> newXPos1 = xColliding.fold(newXPos1) { acc, t ->
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
                l1 = newXPos1 - collider.width / 2,
                r1 = newXPos1 + collider.width / 2,
                b1 = rectBottom,
                t1 = rectTop,
                l2 = tPos.xPos - tCol.width / 2,
                r2 = tPos.xPos + tCol.width / 2,
                b2 = tPos.yPos - tCol.height / 2,
                t2 = tPos.yPos + tCol.height / 2
            )
        }
        when {
            e.velocityY < 0 -> newYPos1 = yColliding.fold(newYPos1) { acc, t ->
                val tPos = t.getComponent(PositionComponent::class)
                val tCol = t.getComponent(ColliderComponent::class)
                velocityComponent.velY = 0f
                land()
                acc.coerceAtLeast(tPos.yPos + tCol.height / 2 + collider.height / 2)
            }

            e.velocityY > 0 -> newYPos1 = yColliding.fold(newYPos1) { acc, t ->
                val tPos = t.getComponent(PositionComponent::class)
                val tCol = t.getComponent(ColliderComponent::class)
                velocityComponent.velY = 0f
                acc.coerceAtMost(tPos.yPos - tCol.height / 2 - collider.height / 2)
            }
        }
        return Pair(newXPos1, newYPos1)
    }

    // TODO when handling collisions effect make sure we don't consume one health thing twice and two bullets don't kill
    //  one killable thing: dude eats one bullet, another goes through, should they ever conflict
    fun detectCollisions(
        entity: Entity,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        ctx: EffectProcessingContext
    ) {
        val entityCollider = entity.getComponent(ColliderComponent::class)
        val collisionsEffects = ctx.entities().all().filter { it.hasComponent(ColliderComponent::class) }.mapNotNull { e ->
            if (e.id == entity.id) return@mapNotNull null
            val collider = e.getComponent(ColliderComponent::class)
            val position = e.getComponent(PositionComponent::class)
            val ex1 = position.xPos - collider.width / 2 - entityCollider.width / 2
            val ex2 = position.xPos + collider.width / 2 + entityCollider.width / 2
            val ey1 = position.yPos - collider.height / 2 - entityCollider.height / 2
            val ey2 = position.yPos + collider.height / 2 + entityCollider.height / 2
            findIntersectionPoint(x1, y1, x2, y2, ex1, ey1, ex2, ey2)?.let { intersection ->
                CollisionEffect.create(entity.id, e.id, intersection.first, intersection.second)
            }
        }
        collisionsEffects.sortedBy { effect ->
            // we want the closest collisions to be resolved first, hence sorting
            (x1 - effect.x) * (x1 - effect.x) + (y1 - effect.y) * (y1 - effect.y)
        }.forEach {
            ctx.fire(it)
        }
    }

    // ChatGPT-generated, better to double check
    fun findIntersectionPoint(
        x1: Float, y1: Float, x2: Float, y2: Float,
        ex1: Float, ey1: Float, ex2: Float, ey2: Float
    ): Pair<Float, Float>? {

        // Check if the line is completely outside the rectangle
        if (x1 <= ex1 && x2 <= ex1 || x1 >= ex2 && x2 >= ex2 || y1 <= ey1 && y2 <= ey1 || y1 >= ey2 && y2 >= ey2) {
            return null
        }

        // Check if the line is vertical (to avoid division by zero)
        if (x1 == x2) {
            // Check if the line is inside the rectangle
            if (x1 > ex1 && x1 < ex2) {
                return Pair(x1, maxOf(ey1, minOf(ey2, maxOf(y1, y2))))
            }
            return null
        }

        // Calculate slope of the line
        val m = (y2 - y1) / (x2 - x1)

        // Calculate y-intercept of the line (y = mx + b => b = y - mx)
        val b = y1 - m * x1

        // Calculate intersection points with the rectangle sides
        val intersectionPoints = mutableListOf<Pair<Float, Float>>()

        // Intersection with left side (x = minX)
        val leftY = m * ex1 + b
        if (leftY > ey1 && leftY < ey2) {
            intersectionPoints.add(Pair(ex1, leftY))
        }

        // Intersection with right side (x = maxX)
        val rightY = m * ex2 + b
        if (rightY > ey1 && rightY < ey2) {
            intersectionPoints.add(Pair(ex2, rightY))
        }

        // Intersection with bottom side (y = minY)
        val bottomX = (ey1 - b) / m
        if (bottomX > ex1 && bottomX < ex2) {
            intersectionPoints.add(Pair(bottomX, ey1))
        }

        // Intersection with top side (y = maxY)
        val topX = (ey2 - b) / m
        if (topX > ex1 && topX < ex2) {
            intersectionPoints.add(Pair(topX, ey2))
        }

        // Return the intersection point closest to (x1, y1)
        return if (intersectionPoints.isNotEmpty()) {
            intersectionPoints.minByOrNull { it.first * it.first + it.second * it.second }
        } else {
            null
        }
    }
}
