package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.config.CottaConfig
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.input.PlayerInput
import kotlin.reflect.KClass

// Implement this to configure your actual game
interface CottaGame {

    val serverSystems: List<KClass<*>>

    val nonPlayerInputProvider: NonPlayerInputProvider
    // how to start
    fun initializeServerState(entities: Entities)

    // TODO make it not possible to add input components, ensure it only operates on static part, etc. More safety.
    fun initializeStaticState(entities: Entities)

    val metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>>

    val config: CottaConfig

    val playerInputKClass: KClass<out PlayerInput>
}
