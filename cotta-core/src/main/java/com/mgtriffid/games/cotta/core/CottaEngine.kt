package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.registry.ComponentsRegistry
import com.mgtriffid.games.cotta.core.serialization.StateSnapper

interface CottaEngine {
    fun getComponentsRegistry(): ComponentsRegistry
    fun getStateSnapper(): StateSnapper
}
