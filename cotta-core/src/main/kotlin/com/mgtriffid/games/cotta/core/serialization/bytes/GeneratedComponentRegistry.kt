package com.mgtriffid.games.cotta.core.serialization.bytes

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import kotlin.reflect.KClass

interface GeneratedComponentRegistry {
    fun getInputComponentKey(inputComponent: KClass<out InputComponent<*>>): ShortComponentKey
    fun getInputComponentByKey(key: Short): KClass<out InputComponent<*>>
    fun getKey(kClass: KClass<out Component<*>>): ShortComponentKey
    fun getDeclaredComponent(kClass: KClass<out Component<*>>): KClass<out Component<*>>
    fun getComponentClassByKey(key: ShortComponentKey): KClass<out Component<*>>
}
