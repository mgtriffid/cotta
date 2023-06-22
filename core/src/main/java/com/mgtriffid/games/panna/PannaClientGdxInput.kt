package com.mgtriffid.games.panna

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import kotlin.reflect.KClass

class PannaClientGdxInput : CottaClientInput {

    private val storage = Storage()
    /**
     * Called for each Entity that has ownedBy == current player AND has some InputComponent.
     */
    override fun <T : InputComponent<T>> input(entity: Entity, clazz: KClass<T>): T {
        when(clazz) {

        }
        return TODO()
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
