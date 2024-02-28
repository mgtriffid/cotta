package com.mgtriffid.games.cotta.client

interface IncomingDeltas {
    fun exists(tick: Long): Boolean
    fun latest(): Long
}
