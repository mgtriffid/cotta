package com.mgtriffid.games.cotta.client

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.input.PlayerInput
import kotlin.reflect.KClass

interface CottaClientInput {
    fun <T : InputComponent<T>> input(
        entity: Entity,
        clazz: KClass<T>
    ): T

    fun input(): PlayerInput
}
