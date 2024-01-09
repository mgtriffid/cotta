package com.mgtriffid.games.cotta.core.clock

interface CottaClock {
    fun time(): Long
    fun delta(): Float
}
