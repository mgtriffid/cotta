package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import kotlin.reflect.KClass

interface CottaClientInput {
    fun <T: InputComponent<T>> input(
        entity: Entity,
        /*metaEntityId: EntityId,*/
        clazz: KClass<T>
        ): T
}