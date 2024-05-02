package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.config.CottaConfig
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.input.NoOpNonPlayerInputProvider
import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

// Implement this to configure your actual game
interface CottaGame {

    val systems: List<CottaSystem>

    val nonPlayerInputProvider: NonPlayerInputProvider
        get() = NoOpNonPlayerInputProvider
    // how to start
    fun initializeServerState(entities: Entities) {}

    // TODO make it not possible to add input components, ensure it only operates on static part, etc. More safety.
    fun initializeStaticState(entities: Entities) {}

    val config: CottaConfig

    val playerInputKClass: KClass<out PlayerInput>

    val inputProcessing: InputProcessing

    val playersHandler: PlayersHandler
}
