package com.mgtriffid.games.panna

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils.atan2Deg360
import com.badlogic.gdx.utils.viewport.Viewport
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.gdx.CottaClientGdxInput
import com.mgtriffid.games.panna.shared.PannaPlayerInput
import com.mgtriffid.games.panna.shared.game.components.WALKING_DIRECTION_LEFT
import com.mgtriffid.games.panna.shared.game.components.WALKING_DIRECTION_NONE
import com.mgtriffid.games.panna.shared.game.components.WALKING_DIRECTION_RIGHT
import com.mgtriffid.games.panna.shared.game.components.WEAPON_PISTOL
import com.mgtriffid.games.panna.shared.game.components.WEAPON_RAILGUN
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PannaClientGdxInput(
    // TODO either remove or use for determining lookAt and other things
    private val viewport: Viewport
) : CottaClientGdxInput {
    var mayJoin = false
    private val storage = Storage()

    override fun input(): PlayerInput {
        return PannaPlayerInput(
            when (if (storage.leftPressed) -1 else 0 + if (storage.rightPressed) 1 else 0) {
                -1 -> WALKING_DIRECTION_LEFT
                1 -> WALKING_DIRECTION_RIGHT
                else -> WALKING_DIRECTION_NONE
            },
            storage.shootPressed,
            storage.jumpPressed,
            storage.lookAt,
            storage.joinPressed,
            storage.switchWeapon
        ).also { clear() }
    }

    override fun accumulate() {
        with(storage) {
            leftPressed = leftPressed || Gdx.input.isKeyPressed(Input.Keys.A)
            rightPressed = rightPressed || Gdx.input.isKeyPressed(Input.Keys.D)
            shootPressed = shootPressed || Gdx.input.isTouched
            jumpPressed = jumpPressed || Gdx.input.isKeyPressed(Input.Keys.SPACE)
            lookAt = getLookAt()
            joinPressed = joinPressed || Gdx.input.isTouched
            switchWeapon = if (switchWeapon == 0.toByte()) {
                when {
                    Gdx.input.isKeyPressed(Input.Keys.NUM_2) -> WEAPON_PISTOL
                    Gdx.input.isKeyPressed(Input.Keys.NUM_3) -> WEAPON_RAILGUN
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

    private fun clear() {
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
