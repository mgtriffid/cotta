package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

// Implement this to configure your actual game
interface CottaGame {
    val serverSystems: List<KClass<*>>
    val serverInputProvider: ServerInputProvider
    // how to start
    fun initializeServerState(state: CottaState)
    val componentClasses: List<KClass<out Component<*>>>
    val inputComponentClasses: List<KClass<out InputComponent<*>>>
}
