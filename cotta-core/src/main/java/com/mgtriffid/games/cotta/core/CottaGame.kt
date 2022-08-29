package com.mgtriffid.games.cotta.core

interface CottaGame {
    fun update()
    fun initialState(): Any // TODO not Any!
    fun calculateNonPlayerInput(state: Any): Any
    fun applyInput(state: Any, input: Any) : Any
}
