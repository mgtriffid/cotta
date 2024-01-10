package com.mgtriffid.games.cotta.core.guice

import com.google.inject.*
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.registry.*
import com.mgtriffid.games.cotta.core.serialization.InputSerialization
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.SnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.MapsInputSerialization
import com.mgtriffid.games.cotta.core.serialization.impl.MapsInputSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.MapsSnapsSerialization
import com.mgtriffid.games.cotta.core.serialization.impl.MapsStateSnapper
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsInputRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsStateRecipe
import kotlin.reflect.KClass

class SerializationModule : Module {
    override fun configure(binder: Binder) {
        with(binder) {
            val stateSnapper = MapsStateSnapper()
            val snapsSerialization = MapsSnapsSerialization()
            val inputSnapper = MapsInputSnapper()
            val inputSerialization = MapsInputSerialization()
            val componentsRegistry = ComponentsRegistryImpl()
            bind(MapsStateSnapper::class.java).toInstance(stateSnapper)
            bind(object : TypeLiteral<StateSnapper<MapsStateRecipe, MapsDeltaRecipe>>() {}).toInstance(stateSnapper)
            bind(MapsSnapsSerialization::class.java).`in`(Scopes.SINGLETON)
            bind(object : TypeLiteral<SnapsSerialization<MapsStateRecipe, MapsDeltaRecipe>>() {}).toInstance(snapsSerialization)
            bind(MapsInputSnapper::class.java).toInstance(inputSnapper)
            bind(object : TypeLiteral<InputSnapper<MapsInputRecipe>>() {}).toInstance(inputSnapper)
            bind(MapsInputSerialization::class.java).toInstance(inputSerialization)
            bind(object : TypeLiteral<InputSerialization<MapsInputRecipe>>() {}).toInstance(inputSerialization)
            bind(ComponentsRegistry::class.java).toInstance(componentsRegistry)
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
            componentsRegistry.addEffectRegistrationListener(object : EffectRegistrationListener {
                override fun onEffectRegistration(effectClass: KClass<out CottaEffect>, descriptor: EffectSpec) {
                    stateSnapper.registerEffect(effectClass, descriptor)
                }
            })
        }
    }
}
