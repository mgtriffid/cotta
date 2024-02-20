package com.mgtriffid.games.cotta.core.registry.impl

import com.esotericsoftware.kryo.Kryo
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.registry.ComponentRegistrationListener
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.EffectRegistrationListener
import com.mgtriffid.games.cotta.core.registry.InputComponentRegistrationListener
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.registry.ShortEffectKey
import com.mgtriffid.games.cotta.core.serialization.IdsRemapper
import com.mgtriffid.games.cotta.core.serialization.bytes.DataClassSerializer
import com.mgtriffid.games.cotta.core.serialization.bytes.ObjectSerializer
import com.mgtriffid.games.cotta.core.serialization.IdsRemapperImpl
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlin.reflect.KClass

class ComponentRegistryImpl @Inject constructor(
    @Named("snapper") private val kryo: Kryo,
    private val idsRemapper: IdsRemapper
) : ComponentRegistry {
    private val inputComponentKeyByClass = HashMap<KClass<out InputComponent<*>>, ShortComponentKey>()
    private var inputComponentClassByKey = ArrayList<KClass<out InputComponent<*>>>()
    private val componentKeyByClass = HashMap<KClass<out Component<*>>, ShortComponentKey>()
    private var componentClassByKey = ArrayList<KClass<out Component<*>>>()

    private val componentRegistrationListeners = ArrayList<ComponentRegistrationListener>()
    private val inputComponentsRegistrationListeners = ArrayList<InputComponentRegistrationListener>()
    private val effectRegistrationListeners = ArrayList<EffectRegistrationListener>()

    override fun getInputComponentKey(inputComponent: KClass<out InputComponent<*>>): ShortComponentKey {
        return inputComponentKeyByClass[inputComponent] ?: throw IllegalArgumentException("No key for $inputComponent")
    }

    override fun getInputComponentClassByKey(key: ShortComponentKey): KClass<out InputComponent<*>> {
        return inputComponentClassByKey[key.key.toInt()]
    }

    override fun getKey(kClass: KClass<out Component<*>>): ShortComponentKey {
        return componentKeyByClass[kClass] ?: throw IllegalArgumentException("No key for $kClass")
    }

    override fun getDeclaredComponent(kClass: KClass<out Component<*>>): KClass<out Component<*>> {
        return getComponentClassByKey(getKey(kClass))
    }

    override fun getComponentClassByKey(key: ShortComponentKey): KClass<out Component<*>> {
        return componentClassByKey[key.key.toInt()]
    }

    override fun registerComponent(
        key: ShortComponentKey,
        kClass: KClass<out Component<*>>,
        kClassImpl: KClass<out Component<*>>
    ) {
        componentKeyByClass[kClass] = key
        componentKeyByClass[kClassImpl] = key
        registerForKryo(kClassImpl)
        registerComponentForRemapping(key, kClass)
        componentClassByKey.add(kClass)
    }

    override fun registerInputComponent(
        key: ShortComponentKey,
        kClass: KClass<out InputComponent<*>>,
        kClassImpl: KClass<out InputComponent<*>>
    ) {
        inputComponentKeyByClass[kClass] = key
        inputComponentKeyByClass[kClassImpl] = key
        registerForKryo(kClassImpl)
        registerInputComponentForRemapping(key, kClass)
        inputComponentClassByKey.add(kClass)
    }

    override fun registerEffect(
        key: ShortEffectKey,
        kClass: KClass<out CottaEffect>,
        kClassImpl: KClass<out CottaEffect>
    ) {
        registerForKryo(kClassImpl)
        registerEffectForRemapping(key, kClass)
        (idsRemapper as IdsRemapperImpl).registerEffect(kClass, createEffectSpec(kClass))
    }

    private fun registerForKryo(kClass: KClass<*>) {
        val serializer = when {
            kClass.isData -> DataClassSerializer(kClass)
            kClass.objectInstance != null -> ObjectSerializer(kClass.objectInstance!!)
            else -> throw IllegalArgumentException("No serializer for $kClass")
        }
        kryo.register(kClass.java, serializer)
    }

    private fun registerComponentForRemapping(key: ShortComponentKey, kClass: KClass<out Component<*>>) {
        (idsRemapper as IdsRemapperImpl).registerComponent(kClass, createComponentSpec(kClass))
    }

    private fun registerInputComponentForRemapping(key: ShortComponentKey, kClass: KClass<out InputComponent<*>>) {
        (idsRemapper as IdsRemapperImpl).registerInputComponent(kClass, createComponentSpec(kClass))
    }

    private fun registerEffectForRemapping(key: ShortEffectKey, kClass: KClass<out CottaEffect>) {
        (idsRemapper as IdsRemapperImpl).registerEffect(kClass, createEffectSpec(kClass))
    }

    override fun addRegistrationListener(listener: ComponentRegistrationListener) {
        componentRegistrationListeners.add(listener)
    }

    override fun addInputComponentRegistrationListener(listener: InputComponentRegistrationListener) {
        inputComponentsRegistrationListeners.add(listener)
    }

    override fun addEffectRegistrationListener(listener: EffectRegistrationListener) {
        effectRegistrationListeners.add(listener)
    }
}
