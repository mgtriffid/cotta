package com.mgtriffid.games.panna

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent.Companion.IDLE
import com.mgtriffid.games.panna.shared.game.components.input.JoinBattleMetaEntityInputComponent.Companion.JOIN_BATTLE
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_DOWN
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_LEFT
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_NONE
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_RIGHT
import com.mgtriffid.games.panna.shared.game.components.input.WALKING_DIRECTION_UP
import com.mgtriffid.games.panna.shared.game.components.input.WalkingInputComponent
import mu.KotlinLogging
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class PannaClientGdxInput : CottaClientInput {
    private var sentJoinBattle = false
    private val storage = Storage()

    /**
     * Called for each Entity that has ownedBy == current player AND has some InputComponent.
     */
    override fun <T : InputComponent<T>> input(entity: Entity, clazz: KClass<T>): T {
        when(clazz) {
            JoinBattleMetaEntityInputComponent::class -> {
                return if (!sentJoinBattle) {
                    sentJoinBattle = true
                    JoinBattleMetaEntityInputComponent.create(JOIN_BATTLE) as T
                } else {
                    // TODO allow to assume blank
                    JoinBattleMetaEntityInputComponent.create(IDLE) as T
                }.also {
                    logger.trace { "Prepared ${JoinBattleMetaEntityInputComponent::class.simpleName} $it" }
                }
            }
            WalkingInputComponent::class -> {
                return WalkingInputComponent.create(
                    when {
                        storage.leftPressed -> WALKING_DIRECTION_LEFT
                        storage.rightPressed -> WALKING_DIRECTION_RIGHT
                        storage.upPressed -> WALKING_DIRECTION_UP
                        storage.downPressed -> WALKING_DIRECTION_DOWN
                        else -> WALKING_DIRECTION_NONE
                    }.also { logger.trace { "WalkingInputComponent created; direction == $it" } }
                ) as T
            }
        }
        throw IllegalArgumentException() // TODO write a reasonable "unregistered component exception"
    }

    fun accumulate() {
        with (storage) {
            leftPressed = leftPressed || Gdx.input.isKeyPressed(Input.Keys.A)
            rightPressed = rightPressed || Gdx.input.isKeyPressed(Input.Keys.D)
            upPressed = upPressed || Gdx.input.isKeyPressed(Input.Keys.W)
            downPressed = downPressed || Gdx.input.isKeyPressed(Input.Keys.S)
        }
    }

    fun clear() {
        with (storage) {
            leftPressed = false
            rightPressed = false
            upPressed = false
            downPressed = false
        }
    }

    class Storage {
        var leftPressed: Boolean = false
        var rightPressed: Boolean = false
        var upPressed: Boolean = false
        var downPressed: Boolean = false
    }
}
