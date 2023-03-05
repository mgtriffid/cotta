package com.mgtriffid.games.cotta.core.impl

import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.registry.ComponentSpec
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistry
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistryImpl
import com.mgtriffid.games.cotta.core.registry.RegistrationListener
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.MapsStateSnapperImpl
import kotlin.reflect.KClass

class CottaEngineImpl : CottaEngine {

    private val componentsRegistry = ComponentsRegistryImpl()
    private val stateSnapper = MapsStateSnapperImpl()

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

    override fun getStateSnapper(): StateSnapper {
        return stateSnapper
    }
}
