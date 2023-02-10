package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component
import kotlin.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

class IdioticSerializationDeserialization {
    private val serializers = HashMap<KClass<*>, Serializer<*>>()

    fun <C : Any> registerComponentClass(kClass: KClass<C>) {
        if (!kClass.isSubclassOf(Component::class)) {
            throw IllegalArgumentException("${kClass.qualifiedName} is not a Component class")
        }
        val companion = kClass.companionObject
            ?: throw IllegalArgumentException("${kClass.qualifiedName} does not have a companion object")
        val companionInstance = kClass.companionObjectInstance ?: throw IllegalArgumentException("Could not find companion instance")
        val factoryMethod = companion.members.find { it.name == "create" }
            ?: throw IllegalArgumentException("${kClass.qualifiedName} has no 'create' method")

        val fields = kClass.declaredMemberProperties.filter { it.hasAnnotation<ComponentData>() }

        val fieldsByName = fields.associateBy { it.name }

        val factoryParameters = factoryMethod.parameters
        val factoryInstanceParameter = factoryParameters.find { it.kind == KParameter.Kind.INSTANCE }
            ?: throw IllegalArgumentException("No instance parameter on factory")
        val valueParameters = factoryParameters.filter { it.kind == KParameter.Kind.VALUE }
        val valueParametersToNames = valueParameters.associateWith { it.name }

        val fieldNames = fieldsByName.keys
        val valueParametersNames = valueParametersToNames.values
        if (fieldNames.toSet() != valueParametersNames.toSet()) {
            throw IllegalArgumentException(
                "Factory method for class '${kClass.qualifiedName}' is misconfigured: fields are " +
                        "${fieldNames.joinToString()}, factory parameters are ${valueParametersNames.joinToString()}}"
            )
        }

        serializers[kClass] = object : Serializer<C> {
            override fun serialize(any: C): Map<String, Any?> {
                return fieldsByName.mapValues { (_, field) -> field.get(any) }
            }

            override fun deserialize(data: Map<String, Any?>, kClass: KClass<C>): C {
                return factoryMethod.callBy(
                    mapOf(factoryInstanceParameter to companionInstance) + valueParameters.associateWith { p ->
                        data[valueParametersToNames[p]]
                    }
                ) as C
            }
        }
    }

    fun <T: Any> serialize(component: T): Map<String, Any?> {
        val kClass = component::class.superclasses.first { serializers.containsKey(it) }
        return (serializers[kClass] as Serializer<T>).serialize(component)
    }

    private fun findFactoryMethod(kClass: KClass<*>) {
        TODO()
    }

    fun <T : Any> deserialize(serialized: Map<String, Any?>, kClass: KClass<T>): T {
        return (serializers[kClass] as Serializer<T>).deserialize(serialized, kClass)
    }

    private interface Serializer<T : Any> {
        fun serialize(any: T): Map<String, Any?>
        fun deserialize(data: Map<String, Any?>, kClass: KClass<T>): T
    }
}
