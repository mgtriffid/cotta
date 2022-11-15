package com.mgtriffid.games.panna

interface PannaConfig {
    val width: Int
    val height: Int
}

object PannaConfigStatic: PannaConfig {
    override val width = 1280
    override val height = 720
}
