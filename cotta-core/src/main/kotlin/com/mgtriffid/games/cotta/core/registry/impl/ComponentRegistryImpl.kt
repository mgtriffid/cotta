package com.mgtriffid.games.cotta.core.registry.impl

import com.esotericsoftware.kryo.Kryo
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.registry.ComponentRegistrationListener
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.EffectRegistrationListener
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.registry.ShortEffectKey
import com.mgtriffid.games.cotta.core.serialization.bytes.DataClassSerializer
import com.mgtriffid.games.cotta.core.serialization.bytes.ObjectSerializer
import jakarta.inject.Inject
import jakarta.inject.Named
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KClass

class ComponentRegistryImpl @Inject constructor(
    @Named("snapper") private val kryo: Kryo,
) : ComponentRegistry {
    private val componentKeyByClass = HashMap<KClass<out Component>, ShortComponentKey>()
    private var componentSpecByKey = ArrayList<ComponentSpec2>()

    private val componentRegistrationListeners = ArrayList<ComponentRegistrationListener>()
    private val effectRegistrationListeners = ArrayList<EffectRegistrationListener>()

    override fun getKey(kClass: KClass<out Component>): ShortComponentKey {
        return componentKeyByClass[kClass] ?: throw IllegalArgumentException("No key for $kClass")
    }

    override fun getDeclaredComponent(kClass: KClass<out Component>): KClass<out Component> {
        return getComponentClassByKey(getKey(kClass))
    }

    override fun getComponentClassByKey(key: ShortComponentKey): KClass<out Component> {
        return componentSpecByKey[key.key.toInt()].kClass
    }

    override fun registerComponent(
        key: ShortComponentKey,
        kClass: KClass<out Component>,
        kClassImpl: KClass<out Component>,
        historical: Boolean
    ) {
        componentKeyByClass[kClass] = key
        componentKeyByClass[kClassImpl] = key
        registerForKryo(kClassImpl)
        componentSpecByKey.add(ComponentSpec2(kClass, historical))
    }

    override fun registerEffect(
        key: ShortEffectKey,
        kClass: KClass<out CottaEffect>,
        kClassImpl: KClass<out CottaEffect>
    ) {
        registerForKryo(kClassImpl)
    }

    override fun isHistorical(key: ShortComponentKey): Boolean {
        return componentSpecByKey[key.key.toInt()].historical
    }

    private fun registerForKryo(kClass: KClass<*>) {
        val serializer = when {
            kClass.isData -> DataClassSerializer(kClass)
            kClass.objectInstance != null -> ObjectSerializer(kClass.objectInstance!!)
            else -> throw IllegalArgumentException("No serializer for $kClass")
        }
        kryo.register(kClass.java, serializer)
    }

    override fun addRegistrationListener(listener: ComponentRegistrationListener) {
        componentRegistrationListeners.add(listener)
    }

    override fun addEffectRegistrationListener(listener: EffectRegistrationListener) {
        effectRegistrationListeners.add(listener)
    }

    override fun getAllComponents(): SortedMap<ShortComponentKey, KClass<out Component>> {
        return componentSpecByKey.mapIndexed { index, spec -> ShortComponentKey(index.toShort()) to spec.kClass }.toMap().toSortedMap(
            Comparator.comparing(ShortComponentKey::key))
    }
}

private data class ComponentSpec2(
    val kClass: KClass<out Component>,
    val historical: Boolean
)
