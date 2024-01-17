package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.utils.now
import com.mgtriffid.games.panna.screens.game.graphics.textures.LeftRightRegions
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.LookingAtComponent
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_LEFT
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_NONE
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_RIGHT
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent

class DudeActor(
    private val regions: ActorFactory.Regions.DudeRegions,
) : PannaActor() {
    private var feetRegion: TextureRegion = regions.feetOnGround.right
    private var eyesRegion: TextureRegion = regions.eyes.lookingStraight.right

    private var walkingState: WalkingState = WalkingState.Standing
    private val animationLength = regions.feetRunning.sumOf { it.first }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch.draw(
            regions.body,
            x - regions.body.regionWidth / 2,
            y - regions.body.regionHeight / 2
        )
        batch.draw(feetRegion, x - feetRegion.regionWidth / 2, y - feetRegion.regionHeight / 2)
        batch.draw(eyesRegion, x - eyesRegion.regionWidth / 2, y - eyesRegion.regionHeight / 2)
    }

    override fun update(entity: Entity) {
        val feet = if (entity.getComponent(JumpingComponent::class).inAir) {
            regions.feetInAir
        } else {
            if (entity.hasComponent(VelocityComponent::class)) {
                chooseFeetOnGroundSprite(entity.getComponent(VelocityComponent::class))
            } else {
                regions.feetOnGround
            }
        }

        val lookAt = entity.getComponent(LookingAtComponent::class).lookAt
        val eyes = when (lookAt) {
            in 0f..30f -> regions.eyes.lookingStraight
            in 30f..150f -> regions.eyes.lookingUp
            in 150f..210f -> regions.eyes.lookingStraight
            in 210f..330f -> regions.eyes.lookingDown
            in 330f..360f -> regions.eyes.lookingDown
            else -> throw IllegalStateException("Look at angle $lookAt is not in range 0..360")
        }
        if (lookAt in 90f..270f) {
            feetRegion = feet.left
            eyesRegion = eyes.left
        } else {
            feetRegion = feet.right
            eyesRegion = eyes.right
        }
    }

    private fun chooseFeetOnGroundSprite(inputComponent: VelocityComponent): LeftRightRegions {
        val velX = inputComponent.velX
        walkingState = walkingState.update(
            when {
                velX == 0f -> WALKING_DIRECTION_NONE
                velX < 0f -> WALKING_DIRECTION_LEFT
                velX > 0f -> WALKING_DIRECTION_RIGHT
                else -> throw IllegalStateException("Unexpected velX $velX")
            }
        )

        return walkingState.let {
            when (it) {
                is WalkingState.Standing -> regions.feetOnGround
                is WalkingState.Walking -> {
                    val frameMs = (now() - it.since) % animationLength
                    var acc = 0
                    for ((duration, regions) in regions.feetRunning) {
                        acc += duration.toInt()
                        if (frameMs <= acc) {
                            return@let regions
                        }
                    }
                    throw IllegalStateException("Frame $frameMs is not in range 0..$acc")
                }
            }
        }
    }

    private sealed interface WalkingState {
        fun update(walkingDirection: Byte): WalkingState

        object Standing : WalkingState {
            override fun update(walkingDirection: Byte): WalkingState {
                return when (walkingDirection) {
                    WALKING_DIRECTION_NONE -> this
                    WALKING_DIRECTION_LEFT -> Walking.Left(now())
                    WALKING_DIRECTION_RIGHT -> Walking.Right(now())
                    else -> throw IllegalStateException("Unknown walking direction: $walkingDirection")
                }
            }
        }
        sealed class Walking(val since: Long) : WalkingState {
            class Left(since: Long) : Walking(since) {
                override fun update(walkingDirection: Byte): WalkingState {
                    return when (walkingDirection) {
                        WALKING_DIRECTION_NONE -> Standing
                        WALKING_DIRECTION_LEFT -> this
                        WALKING_DIRECTION_RIGHT -> Right(now())
                        else -> throw IllegalStateException("Unknown walking direction: $walkingDirection")
                    }
                }
            }

            class Right(since: Long) : Walking(since) {
                override fun update(walkingDirection: Byte): WalkingState {
                    return when (walkingDirection) {
                        WALKING_DIRECTION_NONE -> Standing
                        WALKING_DIRECTION_LEFT -> Left(now())
                        WALKING_DIRECTION_RIGHT -> this
                        else -> throw IllegalStateException("Unknown walking direction: $walkingDirection")
                    }
                }
            }
        }
    }
}
