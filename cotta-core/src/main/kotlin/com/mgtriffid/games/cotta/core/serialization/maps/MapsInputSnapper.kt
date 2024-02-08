package com.mgtriffid.games.cotta.core.serialization.maps

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ComponentKey
import com.mgtriffid.games.cotta.core.registry.ComponentSpec
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.InputSnapper
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapInputComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsEntityInputRecipe
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsInputRecipe
import mu.KotlinLogging
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

private val logger = KotlinLogging.logger {}

class MapsInputSnapper: InputSnapper<MapsInputRecipe> {
    private val snappers = HashMap<ComponentKey, InputComponentSnapper<*>>()

    private val keyByClass = HashMap<KClass<*>, StringComponentKey>()
    private val classByKey = HashMap<StringComponentKey, KClass<InputComponent<*>>>()
    private val factoryMethodsByClass = HashMap<ComponentKey, KCallable<*>>()

    fun <T: InputComponent<T>> registerInputComponent(kClass: KClass<T>, spec: ComponentSpec) {
        logger.debug { "Registering component ${kClass.qualifiedName}, spec has key of ${spec.key}" }
        keyByClass[kClass] = spec.key as StringComponentKey // hack, the fact that maps and componentregistry are connected leaks in here but okay
        registerSnapper(kClass, spec)
        classByKey[spec.key as StringComponentKey] = kClass as KClass<InputComponent<*>>
    }

    private fun <C : InputComponent<C>> registerSnapper(kClass: KClass<C>, spec: ComponentSpec) {
        val companion = kClass.companionObject
            ?: throw IllegalArgumentException("${kClass.qualifiedName} does not have a companion object")
        val companionInstance =
            kClass.companionObjectInstance ?: throw IllegalArgumentException("Could not find companion instance")
        val factoryMethod: KCallable<C> = (companion.members.find { it.name == "create" }
            ?: throw IllegalArgumentException("${kClass.qualifiedName} has no 'create' method")) as KCallable<C>
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
                "Factory method for class '${kClass.qualifiedName}' is misconfigured: fields are " + "${fieldNames.joinToString()}, factory parameters are ${valueParametersNames.joinToString()}}"
            )
        }

        factoryMethodsByClass[spec.key] = factoryMethod

        snappers[spec.key] = InputComponentSnapper(
            key = spec.key as StringComponentKey,
            factoryMethod = factoryMethod,
            factoryInstanceParameter = factoryInstanceParameter,
            companionInstance = companionInstance,
            valueParametersToNames = valueParametersToNames,
            fieldsByName = fieldsByName
        )
    }

    override fun snapInput(input: Map<EntityId, Collection<InputComponent<*>>>): MapsInputRecipe {
        return MapsInputRecipe(input.map { (entityId, components) ->
            packEntityInputs(entityId, components)
        })
    }

    private fun packEntityInputs(
        entityId: EntityId,
        components: Collection<InputComponent<*>>
    ): MapsEntityInputRecipe {
        return MapsEntityInputRecipe(
            entityId,
            components.map { component -> packComponent(component) }
        )
    }

    private fun <C: InputComponent<C>> packComponent(obj: InputComponent<*>): MapInputComponentRecipe {
        obj as C
        return (snappers[getKey(obj)] as InputComponentSnapper<C>).packComponent(obj).also {
            if (it.componentKey == StringComponentKey("JoinBattleMetaEntityInputComponent")) {
                logger.debug { "Packing JoinBattleMetaEntityInputComponent" }
            }
        }
    }

    override fun unpackInputRecipe(recipe: MapsInputRecipe): Map<EntityId, Collection<InputComponent<*>>> {
        return recipe.entityInputs.associate {
            it.entityId to it.inputComponents.map { c ->
                snappers[c.componentKey]?.unpackComponent(c)
                    ?: throw java.lang.IllegalArgumentException("Input Component Snapper not found for component ${c.componentKey}")
            }
        }
    }

    private inner class InputComponentSnapper<C : InputComponent<C>>(
        val key: StringComponentKey,
        val factoryMethod: KCallable<C>,
        val factoryInstanceParameter: KParameter,
        val companionInstance: Any,
        val valueParametersToNames: Map<KParameter, String?>,
        val fieldsByName: Map<String, KProperty1<C, *>>
    ) {
        private val valueParameters: Collection<KParameter> = valueParametersToNames.keys

        fun packComponent(obj: C): MapInputComponentRecipe {
            return MapInputComponentRecipe(componentKey = key, data = fieldsByName.mapValues { (_, field) ->
                try {
                    field.get(obj) ?: throw IllegalStateException("Nullable fields are not allowed")
                } catch (e: Exception) {
                    logger.debug { "Field: ${field.name}, object: $obj" }
                    throw e
                }
            })
        }

        fun unpackComponent(recipe: MapInputComponentRecipe): C {
            val firstParam: Map<KParameter, Any> = mapOf(factoryInstanceParameter to companionInstance)
            val otherParams: Map<KParameter, Any?> = valueParameters.associateWith { p: KParameter ->
                recipe.data[valueParametersToNames[p]]
            }
            return factoryMethod.callBy(
                firstParam + otherParams
            ).also { logger.trace { "Unpacked $it" } }
        }
    }

    private fun getKey(obj: InputComponent<*>): StringComponentKey {
        val kClass = obj::class
        val registeredClass = keyByClass.keys.first { it.isSuperclassOf(kClass) }
        return keyByClass[registeredClass] ?: throw java.lang.IllegalArgumentException("Unexpected type ${kClass.qualifiedName}")
    }
}
