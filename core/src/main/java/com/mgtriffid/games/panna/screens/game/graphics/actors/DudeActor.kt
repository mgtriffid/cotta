package com.mgtriffid.games.panna.screens.game.graphics.actors

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.utils.now
import com.mgtriffid.games.panna.screens.game.graphics.textures.LeftRightRegions
import com.mgtriffid.games.panna.shared.game.components.HealthComponent
import com.mgtriffid.games.panna.shared.game.components.JumpingComponent
import com.mgtriffid.games.panna.shared.game.components.LookingAtComponent
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_LEFT
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_NONE
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_RIGHT
import com.mgtriffid.games.panna.shared.game.components.physics.VelocityComponent

class DudeActor(
    private val regions: ActorFactory.Regions.DudeRegions,
    private val weaponRegions: ActorFactory.Regions.WeaponsRegions,
) : PannaActor {
    private val healthBarActor = HealthBarActor()
    private val weaponActor = WeaponActor()
    override val actor = object : Group() {
        init {
            addActor(DudeBodyActor())
            addActor(healthBarActor)
            addActor(weaponActor)
        }
    }
    private var feetRegion: TextureRegion = regions.feetOnGround.right
    private var eyesRegion: TextureRegion = regions.eyes.lookingStraight.right

    private var walkingState: WalkingState = WalkingState.Standing
    private val animationLength = regions.feetRunning.sumOf { it.first }

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
        updateHealthBar(entity)
        updateWeapon(lookAt)
    }

    private fun updateHealthBar(entity: Entity) {
        if (entity.hasComponent(HealthComponent::class)) {
            healthBarActor.isVisible = true
            val component = entity.getComponent(HealthComponent::class)
            healthBarActor.percentage = component.health.toFloat() / component.max
        } else {
            healthBarActor.isVisible = false
        }
    }

    // TODO make use of actual cursor position not just lookAt. It would make gameplay snappier.
    private fun updateWeapon(lookAt: Float) {
        weaponActor.lookAt = lookAt
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

    private inner class DudeBodyActor : Actor() {
        override fun draw(batch: Batch, parentAlpha: Float) {
            super.draw(batch, parentAlpha)
            batch.draw(
                regions.body,
                -regions.body.regionWidth / 2f,
                -regions.body.regionHeight / 2f
            )
            batch.draw(feetRegion, -feetRegion.regionWidth / 2f, -feetRegion.regionHeight / 2f)
            batch.draw(eyesRegion, -eyesRegion.regionWidth / 2f, -eyesRegion.regionHeight / 2f)
        }
    }

    private inner class HealthBarActor : Actor() {
        var percentage: Float = 1f
        override fun draw(batch: Batch, parentAlpha: Float) {
            super.draw(batch, parentAlpha)
            val positionX = -regions.body.regionWidth / 2f
            val positionY = regions.body.regionHeight / 2f + 4
            batch.draw(
                /* region = */ regions.healthBar.healthBarBackground,
                /* x = */ positionX,
                /* y = */ positionY,
                /* originX = */ 0f,
                /* originY = */ 0f,
                /* width = */ 1f,
                /* height = */ 1f,
                /* scaleX = */ regions.body.regionWidth.toFloat(),
                /* scaleY = */ 4f,
                /* rotation = */ 0f
            )
            batch.draw(
                /* region = */ regions.healthBar.healthBarForeground,
                /* x = */ positionX,
                /* y = */ positionY,
                /* originX = */ 0f,
                /* originY = */ 0f,
                /* width = */ 1f,
                /* height = */ 1f,
                /* scaleX = */ regions.body.regionWidth.toFloat() * percentage,
                /* scaleY = */ 4f,
                /* rotation = */ 0f
            )
        }
    }

    private inner class WeaponActor : Actor() {
        var lookAt: Float = 0f // perhaps Actor#rotation can do the trick

        override fun draw(batch: Batch, parentAlpha: Float) {
            with (weaponRegions.pistol) {
                if (lookAt in 90f..270f) {
                    batch.draw(
                        /* region = */ left,
                        /* x = */ -left.regionWidth.toFloat(),
                        /* y = */ -left.regionHeight / 2f,
                        /* originX = */ left.regionWidth.toFloat(),
                        /* originY = */ right.regionHeight / 2f,
                        /* width = */ left.regionWidth.toFloat(),
                        /* height = */ left.regionHeight.toFloat(),
                        /* scaleX = */ 1f,
                        /* scaleY = */ 1f,
                        /* rotation = */ lookAt + 180f
                    )
                } else {
                    batch.draw(
                        /* region = */ right,
                        /* x = */ 0f,
                        /* y = */ -right.regionHeight / 2f,
                        /* originX = */ 0f,
                        /* originY = */ right.regionHeight / 2f,
                        /* width = */ right.regionWidth.toFloat(),
                        /* height = */ right.regionHeight.toFloat(),
                        /* scaleX = */ 1f,
                        /* scaleY = */ 1f,
                        /* rotation = */ lookAt
                    )
                }
            }
        }
    }
}
