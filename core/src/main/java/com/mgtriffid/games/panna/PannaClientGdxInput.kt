package com.mgtriffid.games.panna

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils.atan2Deg360
import com.badlogic.gdx.math.MathUtils.log
import com.badlogic.gdx.utils.viewport.Viewport
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.panna.shared.game.components.input.*
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent.Companion.IDLE
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent.Companion.JOIN_BATTLE
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class PannaClientGdxInput(
    // TODO either remove or use for determining lookAt and other things
    private val viewport: Viewport
) : CottaClientInput {
    var mayJoin = false
    private val storage = Storage()

    /**
     * Called for each Entity that has ownedBy == current player AND has some InputComponent.
     */
    override fun <T : InputComponent<T>> input(
        entity: Entity,
        clazz: KClass<T>
    ): T {
        when (clazz) {
            // GROOM
            JoinBattleMetaEntityInputComponent::class -> {
                logger.info { "Looking for JoinBattleMetaEntityInputComponent" }
                logger.info { "mayJoin=$mayJoin" }
                return if (mayJoin) {
                    logger.info { "storage.joinPressed=${storage.joinPressed}" }
                    if (storage.joinPressed) {
                        mayJoin = false
                        JoinBattleMetaEntityInputComponent.create(JOIN_BATTLE) as T
                    } else {
                        JoinBattleMetaEntityInputComponent.create(IDLE) as T
                    }
                } else {
                    // TODO allow to assume blank
                    JoinBattleMetaEntityInputComponent.create(IDLE) as T
                }.also {
                    logger.trace { "Prepared ${JoinBattleMetaEntityInputComponent::class.simpleName} $it" }
                }
            }

            CharacterInputComponent::class -> {
                return CharacterInputComponent.create(
                    when {
                        storage.leftPressed -> WALKING_DIRECTION_LEFT
                        storage.rightPressed -> WALKING_DIRECTION_RIGHT
                        else -> WALKING_DIRECTION_NONE
                    }.also { logger.trace { "WalkingInputComponent created; direction == $it" } },
                    storage.jumpPressed,
                    storage.lookAt
                ) as T
            }

            ShootInputComponent::class -> {
                logger.trace { "Providing ${ShootInputComponent::class.simpleName}" }
                return ShootInputComponent.create(
                    storage.shootPressed
                ) as T
            }
        }
        throw IllegalArgumentException() // TODO write a reasonable "unregistered component exception"
    }

    fun accumulate() {
        with(storage) {
            leftPressed = leftPressed || Gdx.input.isKeyPressed(Input.Keys.A)
            rightPressed = rightPressed || Gdx.input.isKeyPressed(Input.Keys.D)
            shootPressed = shootPressed || Gdx.input.isTouched
            jumpPressed = jumpPressed || Gdx.input.isKeyPressed(Input.Keys.SPACE)
            lookAt = getLookAt()
            joinPressed = joinPressed || Gdx.input.isTouched
            switchWeapon = if (switchWeapon == 0.toByte()) {
                when {
                    Gdx.input.isKeyPressed(Input.Keys.NUM_2) -> 2
                    Gdx.input.isKeyPressed(Input.Keys.NUM_3) -> 3
                    else -> 0
                }
            } else switchWeapon
        }
    }

    private fun getLookAt(): Float {
        val x = Gdx.input.x
        val y = Gdx.input.y
        val relX = x - Gdx.graphics.width / 2f
        val relY = Gdx.graphics.height / 2f - y
        return atan2Deg360(relY, relX)
    }

    fun clear() {
        with(storage) {
            leftPressed = false
            rightPressed = false
            shootPressed = false
            jumpPressed = false
            joinPressed = false
            switchWeapon = 0
        }
    }

    class Storage {
        var leftPressed: Boolean = false
        var rightPressed: Boolean = false
        var shootPressed: Boolean = false
        var jumpPressed: Boolean = false
        var lookAt: Float = 0f
        var joinPressed: Boolean = false
        var switchWeapon: Byte = 0
    }
}
