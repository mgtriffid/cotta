package com.mgtriffid.games.panna

import com.mgtriffid.games.cotta.client.CottaClientInput
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent

class PannaClientGdxInput : CottaClientInput {
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
}
