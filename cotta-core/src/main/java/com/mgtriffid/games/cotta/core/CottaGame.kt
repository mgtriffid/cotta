package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.entities.CottaState

// Implement this to configure your actual game
interface CottaGame {
    // First, how this game actually works:
    // how to run
    // how to start
    fun initializeServerState(state: CottaState)

    // what's the type of input


    // Second, how to make this game networked:
    // things like interest management go here
}
