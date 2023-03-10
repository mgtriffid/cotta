package com.mgtriffid.games.cotta.core.impl

import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.registry.ComponentSpec
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistry
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistryImpl
import com.mgtriffid.games.cotta.core.registry.RegistrationListener
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateRecipe
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.MapsSnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.impl.MapsStateRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.MapsStateSnapperImpl
import kotlin.reflect.KClass

class CottaEngineImpl : CottaEngine<MapsStateRecipe, MapsDeltaRecipe> {

    private val componentsRegistry = ComponentsRegistryImpl()
    private val stateSnapper = MapsStateSnapperImpl()
    private val snapsSerialization = MapsSnapsSerialization()

    init {
        componentsRegistry.addRegistrationListener(object : RegistrationListener {
            override fun <T : Component<T>> onComponentRegistration(
                kClass: KClass<T>,
                descriptor: ComponentSpec
            ) {
                stateSnapper.registerComponent(kClass, descriptor)
            }
        })
    }

    override fun getComponentsRegistry(): ComponentsRegistry {
        return componentsRegistry
    }

    override fun getStateSnapper(): StateSnapper<MapsStateRecipe, MapsDeltaRecipe> {
        return stateSnapper
    }

    override fun getSnapsSerialization(): SnapsSerialization<MapsStateRecipe, MapsDeltaRecipe> {
        return snapsSerialization
    }
}
