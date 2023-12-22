package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.registry.ComponentsRegistry
import com.mgtriffid.games.cotta.core.serialization.*

interface CottaEngine<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> {
    fun getComponentsRegistry(): ComponentsRegistry
    fun getStateSnapper(): StateSnapper<SR, DR>
    fun getSnapsSerialization(): SnapsSerialization<SR, DR>
    fun getInputSnapper(): InputSnapper<IR>
    fun getInputSerialization(): InputSerialization<IR>
}
