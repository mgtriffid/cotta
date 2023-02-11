package com.mgtriffid.games.cotta.network.idiotic

import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class IdioticSerializationDeserialization {
    private val idioticComponentsSerializers = IdioticComponentsSerializers()
    private val idioticComponentsDeltaSerializers = IdioticComponentsDeltaSerializers()

    fun <C : Any> registerComponentClass(kClass: KClass<C>) {
        if (!kClass.isSubclassOf(Component::class)) {
            throw IllegalArgumentException("${kClass.qualifiedName} is not a Component class")
        }
        idioticComponentsSerializers.registerComponentClass(kClass)
        idioticComponentsDeltaSerializers.registerComponentClass(kClass)
    }

    fun <T: Any> serialize(component: T): Map<String, Any?> {
        return idioticComponentsSerializers.serialize(component)
    }

    fun <T : Any> deserialize(serialized: Map<String, Any?>, kClass: KClass<T>): T {
        return idioticComponentsSerializers.deserialize(serialized, kClass)
    }

    fun <T: Any> serializeDelta(c0: T, c1: T): Map<String, Any?> {
        return idioticComponentsDeltaSerializers.serializeDelta(c0, c1)
    }

    fun <T: Any> deserializeAndApplyDelta(serializedDelta: Map<String, Any?>, target: T) {
        idioticComponentsDeltaSerializers.deserializeAndApplyDelta(serializedDelta, target)
    }
}
