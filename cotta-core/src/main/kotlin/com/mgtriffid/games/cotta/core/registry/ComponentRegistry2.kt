package com.mgtriffid.games.cotta.core.registry

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import kotlin.reflect.KClass

interface ComponentRegistry2 {
    fun getInputComponentKey(inputComponent: KClass<out InputComponent<*>>): ShortComponentKey
    fun getInputComponentClassByKey(key: ShortComponentKey): KClass<out InputComponent<*>>
    fun getKey(kClass: KClass<out Component<*>>): ShortComponentKey
    fun getDeclaredComponent(kClass: KClass<out Component<*>>): KClass<out Component<*>>
    fun getComponentClassByKey(key: ShortComponentKey): KClass<out Component<*>>
    fun addRegistrationListener(listener: ComponentRegistrationListener)
    fun addInputComponentRegistrationListener(listener: InputComponentRegistrationListener)
    fun addEffectRegistrationListener(listener: EffectRegistrationListener)
    fun registerComponent(
        key: ShortComponentKey,
        kClass: KClass<out Component<*>>,
        kClassImpl: KClass<out Component<*>>
    )

    fun registerInputComponent(
        key: ShortComponentKey,
        kClass: KClass<out InputComponent<*>>,
        kClassImpl: KClass<out InputComponent<*>>
    )

    fun registerEffect(
        key: ShortEffectKey,
        kClass: KClass<out CottaEffect>,
        kClassImpl: KClass<out CottaEffect>
    )
}
