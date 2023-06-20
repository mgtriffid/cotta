package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

interface IncomingInput {
    companion object {
        // [brainless] TODO remove static thing make it a field of a non-static object
        val factoryFunctions = HashMap<KClass<*>, () -> InputComponent<*>>()
    }

    fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>>

    fun inputForEntityAndComponent(entityId: EntityId, component: KClass<*>): InputComponent<*> {
        return inputsForEntities()[entityId]?.find { component.isInstance(it) }
            ?: fallback(component)
    }

    private fun fallback(component: KClass<*>): InputComponent<*> {
        return getFactoryFunction(component)()
    }

    private fun getFactoryFunction(component: KClass<*>): () -> InputComponent<*> {
        if (factoryFunctions.containsKey(component)) {
            return factoryFunctions[component]!!
        }
        val companion = component.companionObject
            ?: throw IllegalArgumentException("${component.qualifiedName} does not have a companion object")
        val companionInstance = component.companionObjectInstance
            ?: throw IllegalArgumentException("Could not find companion instance")
        val factoryMethod: KCallable<*> = (companion.members.find { it.name == "createBlank" }
            ?: throw IllegalArgumentException("${component.qualifiedName} has no 'createBlank' method"))
        val factoryInstanceParameter = factoryMethod.parameters.find { it.kind == KParameter.Kind.INSTANCE }
            ?: throw IllegalArgumentException("No instance parameter on factory")
        val argsMap = mapOf(factoryInstanceParameter to companionInstance)
        return { factoryMethod.callBy(argsMap) as InputComponent<*> }.also {
            factoryFunctions[component] = it
        }
    }
}
