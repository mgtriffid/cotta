package com.mgtriffid.games.cotta.server

interface ComponentDeltasProvider {
    fun atTick(tick: Long): ComponentDeltas
}
