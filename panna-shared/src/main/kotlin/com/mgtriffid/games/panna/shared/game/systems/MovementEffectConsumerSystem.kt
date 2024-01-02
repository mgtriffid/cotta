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
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent
import com.mgtriffid.games.panna.shared.game.components.physics.ColliderComponent
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent
import com.mgtriffid.games.panna.shared.game.effects.MovementEffect
import com.mgtriffid.games.panna.shared.utils.Orientation
import com.mgtriffid.games.panna.shared.utils.intersect
import com.mgtriffid.games.panna.shared.utils.orientation
import javax.swing.text.Position
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt

private val logger = mu.KotlinLogging.logger {}

@Predicted
class MovementEffectConsumerSystem : EffectsConsumerSystem {
    override fun handle(e: CottaEffect, ctx: EffectProcessingContext) {
        if (e is MovementEffect) {
            logger.info { "Received MovementEffect: $e" }
            val entity = ctx.entities().get(e.entityId)
            val velocityComponent = entity.getComponent(VelocityComponent::class)
            val land: () -> Unit = if (entity.hasComponent(JumpingComponent::class)) {
                {
                    entity.getComponent(JumpingComponent::class).inAir = false
                    logger.info { "Landed!" }
                }
            } else { {
                logger.info { "Landed! but who cares" }
            } }
            if (entity.hasComponent(PositionComponent::class)) {
                val position = entity.getComponent(PositionComponent::class)
                var newXPos = position.xPos + (e.velocityX * ctx.clock().delta()).roundToInt()
                var newYPos = position.yPos + (e.velocityY * ctx.clock().delta()).roundToInt()

                when {
                    e.velocityX < 0 -> position.orientation = ORIENTATION_LEFT
                    e.velocityX > 0 -> position.orientation = ORIENTATION_RIGHT
                }

                // TODO indexed query
                if (entity.hasComponent(ColliderComponent::class)) {
                    val collider = entity.getComponent(ColliderComponent::class)
                    val rectLeft = Math.min(position.xPos, newXPos) - collider.width / 2
                    val rectRight = Math.max(position.xPos, newXPos) + collider.width / 2
                    val rectTop = Math.max(position.yPos, newYPos) + collider.height / 2
                    val rectBottom = Math.min(position.yPos, newYPos) - collider.height / 2
                    logger.info { "rectLeft = $rectLeft, rectRight = $rectRight, rectTop = $rectTop, rectBottom = $rectBottom" }

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
                            velocityComponent.velX = 0
                            acc.coerceAtMost(tPos.xPos - tCol.width / 2 - collider.width / 2)
                        }
                        e.velocityX < 0 -> newXPos = xColliding.fold(newXPos) { acc, t ->
                            val tPos = t.getComponent(PositionComponent::class)
                            val tCol = t.getComponent(ColliderComponent::class)
                            velocityComponent.velX = 0
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
                            velocityComponent.velY = 0
                            land()
                            acc.coerceAtLeast(tPos.yPos + tCol.height / 2 + collider.height / 2)
                        }
                        e.velocityY > 0 -> newYPos = yColliding.fold(newYPos) { acc, t ->
                            val tPos = t.getComponent(PositionComponent::class)
                            val tCol = t.getComponent(ColliderComponent::class)
                            velocityComponent.velY = 0
                            acc.coerceAtMost(tPos.yPos - tCol.height / 2 - collider.height / 2)
                        }
                    }

                    // <editor-fold desc="Fucked up attempt to resolve collisions">
                    /*val intersecting = terrainBlocks.filter {
                        val terrainPosition = it.getComponent(PositionComponent::class)
                        val terrainCollider = it.getComponent(ColliderComponent::class)
                        val terrainLeft = terrainPosition.xPos - terrainCollider.width / 2
                        val terrainRight = terrainPosition.xPos + terrainCollider.width / 2
                        val terrainTop = terrainPosition.yPos + terrainCollider.height / 2
                        val terrainBottom = terrainPosition.yPos - terrainCollider.height / 2
                        val ret =
                            rectLeft < terrainRight && rectRight > terrainLeft && rectBottom < terrainTop && rectTop > terrainBottom
                        if (ret) {
                            logger.info { "Terrain: left = $terrainLeft, right = $terrainRight, top = $terrainTop, bottom = $terrainBottom" }
                        }
                        ret
                    }
                    logger.info {
                        "Found ${intersecting.size} intersecting blocks: ${
                            intersecting.joinToString {
                                it.getComponent(
                                    PositionComponent::class
                                ).let { "[${it.xPos}, ${it.yPos}]" }
                            }
                        }"
                    }
                    var clampedX = newXPos
                    var clampedY = newYPos

                    intersecting.forEach { it ->
                        val p = it.getComponent(PositionComponent::class)
                        val c = it.getComponent(ColliderComponent::class)
                        when {
                            e.velocityX > 0 && e.velocityY > 0 -> {
                                // moving up and right
                                val statCornerX = p.xPos - c.width / 2
                                val statCornerY = p.yPos - c.height / 2
                                val movCornerX1 = position.xPos + collider.width / 2
                                val movCornerY1 = position.yPos + collider.height / 2
                                val movCornerX2 = newXPos + collider.width / 2
                                val movCornerY2 = newYPos + collider.height / 2
                                when (orientation(movCornerX1, movCornerY1, movCornerX2, movCornerY2, statCornerX, statCornerY)) {
                                    Orientation.CLOCKWISE -> {
                                        // slide along left side; collide horizontally
                                        clampedX = clampedX.coerceAtMost(statCornerX - collider.width / 2)
                                        velocityComponent.velX = 0
                                    }
                                    Orientation.COUNTERCLOCKWISE -> {
                                        // slide along bottom side; collide vertically
                                        clampedY = clampedY.coerceAtMost(statCornerY - collider.height / 2)
                                        velocityComponent.velY = 0
                                    }
                                    Orientation.COLLINEAR -> {
                                        // hit exactly corner
                                        clampedX = clampedX.coerceAtMost(statCornerX - collider.width / 2)
                                        clampedY = clampedY.coerceAtMost(statCornerY - collider.height / 2)
                                        velocityComponent.velX = 0
                                        velocityComponent.velY = 0
                                    }
                                }
                            }
                            e.velocityX > 0 && e.velocityY < 0 -> {
                                // moving down and right
                                val statCornerX = p.xPos - c.width / 2
                                val statCornerY = p.yPos + c.height / 2
                                val movCornerX1 = position.xPos + collider.width / 2
                                val movCornerY1 = position.yPos - collider.height / 2
                                val movCornerX2 = newXPos + collider.width / 2
                                val movCornerY2 = newYPos - collider.height / 2
                                when (orientation(movCornerX1, movCornerY1, movCornerX2, movCornerY2, statCornerX, statCornerY)) {
                                    Orientation.CLOCKWISE -> {
                                        // slide along top side; collide vertically
                                        clampedY = clampedY.coerceAtLeast(statCornerY + collider.height / 2)
                                        velocityComponent.velY = 0
                                        land()
                                    }
                                    Orientation.COUNTERCLOCKWISE -> {
                                        // slide along left side; collide horizontally
                                        clampedX = clampedX.coerceAtMost(statCornerX - collider.width / 2)
                                        velocityComponent.velX = 0
                                    }
                                    Orientation.COLLINEAR -> {
                                        // hit exactly corner
                                        clampedX = clampedX.coerceAtMost(statCornerX - collider.width / 2)
                                        clampedY = clampedY.coerceAtLeast(statCornerY + collider.height / 2)
                                        velocityComponent.velX = 0
                                        velocityComponent.velY = 0
                                        land()
                                    }
                                }
                            }
                            e.velocityX < 0 && e.velocityY > 0 -> {
                                // moving up and left
                                val statCornerX = p.xPos + c.width / 2
                                val statCornerY = p.yPos - c.height / 2
                                val movCornerX1 = position.xPos - collider.width / 2
                                val movCornerY1 = position.yPos + collider.height / 2
                                val movCornerX2 = newXPos - collider.width / 2
                                val movCornerY2 = newYPos + collider.height / 2
                                when (orientation(movCornerX1, movCornerY1, movCornerX2, movCornerY2, statCornerX, statCornerY)) {
                                    Orientation.CLOCKWISE -> {
                                        // slide along bottom side; collide vertically
                                        clampedY = clampedY.coerceAtMost(statCornerY + collider.width / 2)
                                        velocityComponent.velY = 0
                                    }
                                    Orientation.COUNTERCLOCKWISE -> {
                                        // slide along right side; collide horizontally
                                        clampedX = clampedX.coerceAtLeast(statCornerY - collider.height / 2)
                                        velocityComponent.velX = 0
                                    }
                                    Orientation.COLLINEAR -> {
                                        // hit exactly corner
                                        clampedY = clampedY.coerceAtMost(statCornerY + collider.width / 2)
                                        clampedX = clampedX.coerceAtLeast(statCornerY - collider.height / 2)
                                        velocityComponent.velX = 0
                                        velocityComponent.velY = 0
                                    }
                                }
                            }
                            e.velocityX < 0 && e.velocityY < 0 -> {
                                // moving down and left
                                val statCornerX = p.xPos + c.width / 2
                                val statCornerY = p.yPos + c.height / 2
                                val movCornerX1 = position.xPos - collider.width / 2
                                val movCornerY1 = position.yPos - collider.height / 2
                                val movCornerX2 = newXPos - collider.width / 2
                                val movCornerY2 = newYPos - collider.height / 2
                                when (orientation(movCornerX1, movCornerY1, movCornerX2, movCornerY2, statCornerX, statCornerY)) {
                                    Orientation.CLOCKWISE -> {
                                        // slide along top side; collide vertically
                                        clampedY = clampedY.coerceAtLeast(statCornerY + collider.height / 2)
                                        velocityComponent.velY = 0
                                        land()
                                    }
                                    Orientation.COUNTERCLOCKWISE -> {
                                        // slide along right side; collide horizontally
                                        clampedX = clampedX.coerceAtLeast(statCornerX + collider.width / 2)
                                        velocityComponent.velX = 0
                                    }
                                    Orientation.COLLINEAR -> {
                                        // hit exactly corner
                                        clampedY = clampedY.coerceAtLeast(statCornerY + collider.height / 2)
                                        clampedX = clampedX.coerceAtLeast(statCornerX + collider.width / 2)
                                        velocityComponent.velX = 0
                                        velocityComponent.velY = 0
                                        land()
                                    }
                                }
                            }
                            e.velocityX > 0 && e.velocityY == 0 -> {
                                // moving right
                                clampedX = clampedX.coerceAtMost(p.xPos - c.width / 2 - collider.width / 2)
                                velocityComponent.velX = 0
                            }
                            e.velocityX < 0 && e.velocityY == 0 -> {
                                // moving left
                                clampedX = clampedX.coerceAtLeast(p.xPos + c.width / 2 + collider.width / 2)
                                velocityComponent.velX = 0
                            }
                            e.velocityX == 0 && e.velocityY > 0 -> {
                                // moving up
                                clampedY = clampedY.coerceAtMost(p.yPos - c.height / 2 - collider.height / 2)
                                velocityComponent.velY = 0
                            }
                            e.velocityX == 0 && e.velocityY < 0 -> {
                                // moving down
                                clampedY = clampedY.coerceAtLeast(p.yPos + c.height / 2 + collider.height / 2)
                                velocityComponent.velY = 0
                                land()
                            }
                        }
                    }
                    newXPos = clampedX
                    newYPos = clampedY*/
                    // </editor-fold>
                }

                position.xPos = newXPos
                position.yPos = newYPos

                logger.info { "position.xPos = ${position.xPos}, position.yPos = ${position.yPos}" }

                if (position.xPos < -50 || position.xPos > 1000 || position.yPos < -100 || position.yPos > 2000) {
                    ctx.entities().remove(e.entityId)
                }
            }
        }
    }
}
