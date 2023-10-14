package com.mgtriffid.games.cotta.core.impl

import com.mgtriffid.games.cotta.core.CottaEngine
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.registry.ComponentSpec
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistry
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistryImpl
import com.mgtriffid.games.cotta.core.registry.ComponentRegistrationListener
import com.mgtriffid.games.cotta.core.registry.InputComponentRegistrationListener
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.MapsInputSerialization
import com.mgtriffid.games.cotta.core.serialization.impl.MapsInputSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.MapsSnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsStateRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.MapsStateSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsInputRecipe
import kotlin.reflect.KClass

class CottaEngineImpl : CottaEngine<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe> {

    private val componentsRegistry = ComponentsRegistryImpl()
    private val stateSnapper = MapsStateSnapper()
    private val snapsSerialization = MapsSnapsSerialization()
    private val inputSnapper = MapsInputSnapper()
    private val inputSerialization = MapsInputSerialization()

    init {
        componentsRegistry.addRegistrationListener(object : ComponentRegistrationListener {
            override fun <T : Component<T>> onComponentRegistration(
                kClass: KClass<T>,
                descriptor: ComponentSpec
            ) {
                stateSnapper.registerComponent(kClass, descriptor)
            }
        })
        componentsRegistry.addInputComponentRegistrationListener(object : InputComponentRegistrationListener {
            override fun <T : InputComponent<T>> onInputComponentRegistration(
                kClass: KClass<T>,
                descriptor: ComponentSpec
            ) {
                inputSnapper.registerInputComponent(kClass, descriptor)
            }
        })
        componentsRegistry.addInputComponentRegistrationListener(object : InputComponentRegistrationListener {
            override fun <T : InputComponent<T>> onInputComponentRegistration(
                kClass: KClass<T>,
                descriptor: ComponentSpec
            ) {
                stateSnapper.registerInputComponent(kClass, descriptor)
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

    override fun getInputSnapper(): InputSnapper<MapsInputRecipe> {
        return inputSnapper
    }

    override fun getInputSerialization(): InputSerialization<MapsInputRecipe> {
        return inputSerialization
    }
}
