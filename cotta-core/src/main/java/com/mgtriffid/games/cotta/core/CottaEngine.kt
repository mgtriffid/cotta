package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.registry.ComponentsRegistry
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.serialization.StateSnapper

interface CottaEngine<SR: StateRecipe, DR: DeltaRecipe> {
    fun getComponentsRegistry(): ComponentsRegistry
    fun getStateSnapper(): StateSnapper<SR, DR>
    fun getSnapsSerialization(): SnapsSerialization<SR, DR>
}
