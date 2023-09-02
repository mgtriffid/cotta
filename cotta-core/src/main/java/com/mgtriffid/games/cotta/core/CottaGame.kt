package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.InputComponent
import kotlin.reflect.KClass

// Implement this to configure your actual game
interface CottaGame {
    val serverSystems: List<KClass<*>>
    val serverInputProvider: ServerInputProvider
    // how to start
    fun initializeServerState(state: CottaState)
    val componentClasses: Set<KClass<out Component<*>>>
    val inputComponentClasses: Set<KClass<out InputComponent<*>>>
    val metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>>
    val config: CottaConfig
}
