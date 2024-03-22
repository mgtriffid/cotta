package com.mgtriffid.games.cotta.core.registry

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.reflect.KClass

interface ComponentRegistry {
    fun getKey(kClass: KClass<out Component<*>>): ShortComponentKey
    fun getDeclaredComponent(kClass: KClass<out Component<*>>): KClass<out Component<*>>
    fun getComponentClassByKey(key: ShortComponentKey): KClass<out Component<*>>
    fun addRegistrationListener(listener: ComponentRegistrationListener)
    fun addEffectRegistrationListener(listener: EffectRegistrationListener)
    fun registerComponent(
        key: ShortComponentKey,
        kClass: KClass<out Component<*>>,
        kClassImpl: KClass<out Component<*>>,
        historical: Boolean
    )

    fun registerEffect(
        key: ShortEffectKey,
        kClass: KClass<out CottaEffect>,
        kClassImpl: KClass<out CottaEffect>
    )

    fun isHistorical(key: ShortComponentKey): Boolean
}
