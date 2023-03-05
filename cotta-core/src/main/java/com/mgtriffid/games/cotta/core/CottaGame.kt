package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

// Implement this to configure your actual game
interface CottaGame {
    // First, how this game actually works:
    // how to run
    val serverSystems: List<KClass<*>>
    // how to start
    fun initializeServerState(state: CottaState)
    val componentClasses: List<KClass<out Component<*>>>

    // what's the type of input


    // Second, how to make this game networked:
    // things like interest management go here
}
