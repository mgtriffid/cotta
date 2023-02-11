package com.mgtriffid.games.cotta.network.idiotic

import com.mgtriffid.games.cotta.ComponentData
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.superclasses

class IdioticComponentsDeltaSerializers {
    private val serializers = HashMap<KClass<*>, DeltaSerializer<*>>()

    fun <T: Any> registerComponentClass(kClass: KClass<T>) {

        val fields = kClass.declaredMemberProperties.filter {
            it.hasAnnotation<ComponentData>()
        }.filterIsInstance<KMutableProperty1<T, *>>()

        val fieldsByName = fields.associateBy { it.name }

        serializers[kClass] = object: DeltaSerializer<T> {
            override fun serialize(c0: T, c1: T): Map<String, Any?> {
                return fieldsByName.mapNotNull { (name, field) ->
                    val v0 = field.get(c0)
                    val v1 = field.get(c1)
                    if (v0 != v1) {
                        Pair(name, v1)
                    } else {
                        null
                    }
                }.toMap()
            }

            override fun apply(delta: Map<String, Any?>, target: T) {
                delta.forEach { (name, value) ->
                    (fieldsByName[name]!! as KMutableProperty1<T, Any?>).set(target, value)
                }
            }
        }
    }

    fun <T: Any> serializeDelta(c0: T, c1: T): Map<String, Any?> {
        val kClass = c0::class.superclasses.first { serializers.containsKey(it) }
        return (serializers[kClass] as IdioticComponentsDeltaSerializers.DeltaSerializer<T>).serialize(c0, c1)
    }

    fun <T: Any> deserializeAndApplyDelta(serializedDelta: Map<String, Any?>, target: T) {
        val kClass = target::class.superclasses.first { serializers.containsKey(it) }
        return (serializers[kClass] as DeltaSerializer<T>).apply(serializedDelta, target)
    }



    private interface DeltaSerializer<T: Any> {
        fun serialize(c0: T, c1:T) : Map<String, Any?>
        fun apply(delta: Map<String, Any?>, target: T)
    }
}
