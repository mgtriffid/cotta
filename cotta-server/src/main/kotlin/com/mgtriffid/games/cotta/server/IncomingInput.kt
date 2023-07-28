package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import mu.KotlinLogging
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

private val logger = KotlinLogging.logger {}

interface IncomingInput {
    companion object {
        // [brainless] TODO remove static thing make it a field of a non-static object
        val factoryFunctions = HashMap<KClass<*>, () -> InputComponent<*>>()
    }

    fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>>

    fun inputForEntityAndComponent(entityId: EntityId, component: KClass<*>): InputComponent<*> {
        logger.debug { "Getting input for entity $entityId and component ${component.qualifiedName}" }
        val input = inputsForEntities()[entityId]?.find { component.isInstance(it) }
        if (input != null) {
            logger.debug { "Input found" }
            return input
        } else {
            logger.debug { "Input not found, falling back" }
            return fallback(component)
        }
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
