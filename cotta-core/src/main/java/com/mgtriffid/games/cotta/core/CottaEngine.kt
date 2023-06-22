package com.mgtriffid.games.cotta.core

import com.mgtriffid.games.cotta.core.registry.ComponentsRegistry
import com.mgtriffid.games.cotta.core.serialization.DeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.InputRecipe
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.serialization.StateSnapper

interface CottaEngine<SR: StateRecipe, DR: DeltaRecipe, IR: InputRecipe> {
    fun getComponentsRegistry(): ComponentsRegistry
    fun getStateSnapper(): StateSnapper<SR, DR>
    fun getSnapsSerialization(): SnapsSerialization<SR, DR>
    fun getInputSnapper(): InputSnapper<IR>
    fun getInputSerialization(): InputSerialization<IR>
}
