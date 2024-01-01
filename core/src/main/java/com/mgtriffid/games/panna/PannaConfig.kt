package com.mgtriffid.games.panna

interface PannaConfig {
    val width: Int
    val height: Int
}

object PannaConfigStatic: PannaConfig {
    override val width = 960
    override val height = 540
}
