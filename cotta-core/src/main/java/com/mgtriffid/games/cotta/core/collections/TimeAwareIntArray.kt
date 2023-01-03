package com.mgtriffid.games.cotta.core.collections

class TimeAwareIntMap<V: Any>(val historyLength: Int) {
    private var tick: Long = 0L

    fun advance() {

    }

    fun set(id: Int, value: V) {

    }

    fun get(id: Int): V {
        TODO()
    }

    fun getAtTick(tick: Long) {

    }

    fun remove(id: Int) {

    }

    fun currentTick(): Long {
        return tick
    }
}
