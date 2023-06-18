package com.mgtriffid.games.panna

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent

class PannaClientGdxInput : CottaClientInput {

    val storage = Storage()
    /**
     * Called for each Entity that has ownedBy == current player AND has some InputComponent.
     */
    override fun input(entity: Entity, metaEntityId: EntityId): List<InputComponent<*>> {
        when {
/*
            entity.id == metaEntityId -> {
                return listOf(PannaMetaEntityInputComponent(LET_DUDE_ENTER_THE_GAME))
            }
*/
/*
            entity.hasComponent(BattlingDudeComponent::class) -> {
                return listOf(getInputFromControl())
            }
*/
        }
        return emptyList()
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
