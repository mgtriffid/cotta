package com.mgtriffid.games.cotta.core.serialization.impl

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.registry.*
import com.mgtriffid.games.cotta.core.serialization.IdsRemapper
import mu.KotlinLogging
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

private val logger = KotlinLogging.logger {}

class IdsRemapperImpl : IdsRemapper {

    private var componentsRemappers = mutableMapOf<ComponentKey, ComponentRemapper>()
    private var inputComponentsRemappers = mutableMapOf<ComponentKey, InputComponentRemapper>()
    private var effectsRemappers = mutableMapOf<EffectKey, EffectRemapper>()

    private val keyByClass = HashMap<KClass<*>, StringComponentKey>()

    private val inputComponentsKeyByClass = HashMap<KClass<*>, StringComponentKey>()
    private val effectsKeyByClass = HashMap<KClass<*>, StringEffectKey>()
    override fun remap(c: Component<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): Component<*> {
        // GROOM we have null-safe and we have IdentityThing. Redundant.
        return componentsRemappers[getKey(c)]?.remap(c, ids) ?: c
    }

    override fun remap(e: CottaEffect, ids: (PredictedEntityId) -> AuthoritativeEntityId?): CottaEffect {
        return effectsRemappers[getKey(e)]?.remap(e, ids) ?: e
    }

    override fun remap(ic: InputComponent<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): InputComponent<*> {
        return inputComponentsRemappers[getInputComponentKey(ic::class)]?.remap(ic, ids) ?: ic
    }

    fun <T : Component<T>> registerComponent(kClass: KClass<T>, spec: ComponentSpec) {
        logger.debug { "Registering component ${kClass.qualifiedName}, spec has key of ${spec.key}" }
        keyByClass[kClass] = spec.key as StringComponentKey // hack, the fact that maps and componentregistry are connected leaks in here but okay
        registerComponentRemapper(kClass, spec)
    }

    private fun <C : Component<C>> registerComponentRemapper(kClass: KClass<C>, spec: ComponentSpec) {
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

        // GROOM extract this check before adding all those listeners for ComponentRegistry: checks are common.
        if (fieldNames.toSet() != valueParametersNames.toSet()) {
            throw IllegalArgumentException(
                "Factory method for class '${kClass.qualifiedName}' is misconfigured: fields are " + "${fieldNames.joinToString()}, factory parameters are ${valueParametersNames.joinToString()}}"
            )
        }

        componentsRemappers[spec.key] = if (fields.none { it.returnType == EntityId::class.createType() }) {
            IdentityComponentRemapper
        } else {
            ComponentRemapperImpl(
                factoryInstanceParameter,
                factoryMethod,
                valueParametersToNames,
                fieldsByName,
                companionInstance
            )
        }
    }

    fun <T : InputComponent<T>> registerInputComponent(kClass: KClass<T>, spec: ComponentSpec) {
        logger.debug { "Registering input component ${kClass.qualifiedName}, spec has key of ${spec.key}" }
        inputComponentsKeyByClass[kClass] = spec.key as StringComponentKey // hack, the fact that maps and componentregistry are connected leaks in here but okay

        registerInputComponentRemapper(kClass, spec)
    }

    fun <C: InputComponent<C>> registerInputComponentRemapper(kClass: KClass<C>, spec: ComponentSpec) {
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

        // GROOM extract this check before adding all those listeners for ComponentRegistry: checks are common.
        if (fieldNames.toSet() != valueParametersNames.toSet()) {
            throw IllegalArgumentException(
                "Factory method for class '${kClass.qualifiedName}' is misconfigured: fields are " + "${fieldNames.joinToString()}, factory parameters are ${valueParametersNames.joinToString()}}"
            )
        }

        inputComponentsRemappers[spec.key] = if (fields.none { it.returnType == EntityId::class.createType() }) {
            IdentityInputComponentRemapper
        } else {
            InputComponentRemapperImpl(
                factoryInstanceParameter,
                factoryMethod,
                valueParametersToNames,
                fieldsByName,
                companionInstance
            )
        }
    }

    fun <T : CottaEffect> registerEffect(kClass: KClass<T>, spec: EffectSpec) {
        logger.debug { "Registering effect ${kClass.qualifiedName}" }
        effectsKeyByClass[kClass] = spec.key as StringEffectKey // hack, the fact that maps and componentregistry are connected leaks in here but okay

        registerEffectMapper(kClass, spec) // hack, the fact that maps and componentregistry are connected leaks in here but okay
    }

    private fun <T : CottaEffect> registerEffectMapper(kClass: KClass<T>, spec: EffectSpec) {
        val companion = kClass.companionObject
            ?: throw IllegalArgumentException("${kClass.qualifiedName} does not have a companion object")
        val companionInstance =
            kClass.companionObjectInstance ?: throw IllegalArgumentException("Could not find companion instance")
        val factoryMethod: KCallable<CottaEffect> = (companion.members.find { it.name == "create" }
            ?: throw IllegalArgumentException("${kClass.qualifiedName} has no 'create' method")) as KCallable<CottaEffect>
        val fields = kClass.declaredMemberProperties.filter { it.hasAnnotation<EffectData>() }

        val fieldsByName = fields.associateBy { it.name }

        val factoryParameters = factoryMethod.parameters
        val factoryInstanceParameter = factoryParameters.find { it.kind == KParameter.Kind.INSTANCE }
            ?: throw IllegalArgumentException("No instance parameter on factory")
        val valueParameters = factoryParameters.filter { it.kind == KParameter.Kind.VALUE }
        val valueParametersToNames = valueParameters.associateWith { it.name }

        val fieldNames = fieldsByName.keys
        val valueParametersNames = valueParametersToNames.values

        // GROOM extract this check before adding all those listeners for ComponentRegistry: checks are common.
        if (fieldNames.toSet() != valueParametersNames.toSet()) {
            throw IllegalArgumentException(
                "Factory method for class '${kClass.qualifiedName}' is misconfigured: fields are " + "${fieldNames.joinToString()}, factory parameters are ${valueParametersNames.joinToString()}}"
            )
        }

        effectsRemappers[spec.key] = if (fields.none { it.returnType == EntityId::class.createType() }) {
            IdentityEffectRemapper
        } else {
            EffectRemapperImpl(
                factoryInstanceParameter,
                factoryMethod,
                valueParametersToNames,
                fieldsByName,
                companionInstance
            )
        }
    }


    private interface ComponentRemapper {
        fun remap(c: Component<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): Component<*>
    }

    private class ComponentRemapperImpl<C : Component<C>>(
        private val factoryInstanceParameter: KParameter,
        private val factoryMethod: KCallable<C>,
        private val valueParametersToNames: Map<KParameter, String?>,
        private val fieldsByName: Map<String, KProperty1<C, *>>,
        private val companionInstance: Any
    ) : ComponentRemapper {

        override fun remap(c: Component<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): Component<*> {
            val firstParam: Map<KParameter, Any> = mapOf(factoryInstanceParameter to companionInstance)
            val otherParams: Map<KParameter, Any?> = valueParametersToNames.mapValues { (param, name) ->
                val field = fieldsByName[name]
                val value = field?.getter?.call(c)
                if (value is PredictedEntityId) {
                    val remapped = ids(value) ?: value // TODO we actually can't just use PredictedEntityId anyway. Need some assertions or fallback to something like blank
                    logger.trace { "Remapping $value to $remapped" }
                    remapped
                } else {
                    value
                }
            }
            return factoryMethod.callBy(firstParam + otherParams)
        }
    }

    private object IdentityComponentRemapper : ComponentRemapper {
        override fun remap(c: Component<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): Component<*> {
            return c
        }
    }

    private interface InputComponentRemapper {
        fun remap(ic: InputComponent<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): InputComponent<*>
    }

    private class InputComponentRemapperImpl<C : InputComponent<C>>(
        private val factoryInstanceParameter: KParameter,
        private val factoryMethod: KCallable<C>,
        private val valueParametersToNames: Map<KParameter, String?>,
        private val fieldsByName: Map<String, KProperty1<C, *>>,
        private val companionInstance: Any
    ) : InputComponentRemapper {

        override fun remap(ic: InputComponent<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): InputComponent<*> {
            val firstParam: Map<KParameter, Any> = mapOf(factoryInstanceParameter to companionInstance)
            val otherParams: Map<KParameter, Any?> = valueParametersToNames.mapValues { (param, name) ->
                val field = fieldsByName[name]
                val value = field?.getter?.call(ic)
                if (value is PredictedEntityId) {
                    val remapped = ids(value) ?: value // TODO we actually can't just use PredictedEntityId anyway. Need some assertions or fallback to something like blank
                    logger.trace { "Remapping $value to $remapped" }
                    remapped
                } else {
                    value
                }
            }
            return factoryMethod.callBy(firstParam + otherParams)
        }
    }

    private object IdentityInputComponentRemapper : InputComponentRemapper {
        override fun remap(ic: InputComponent<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): InputComponent<*> {
            return ic
        }
    }

    interface EffectRemapper {
        fun remap(e: CottaEffect, ids: (PredictedEntityId) -> AuthoritativeEntityId?): CottaEffect
    }

    private object IdentityEffectRemapper : EffectRemapper {
        override fun remap(e: CottaEffect, ids: (PredictedEntityId) -> AuthoritativeEntityId?): CottaEffect {
            return e
        }
    }

    private class EffectRemapperImpl<C: CottaEffect>(
        private val factoryInstanceParameter: KParameter,
        private val factoryMethod: KCallable<CottaEffect>,
        private val valueParametersToNames: Map<KParameter, String?>,
        private val fieldsByName: Map<String, KProperty1<C, *>>,
        private val companionInstance: Any
    ) : EffectRemapper {
        override fun remap(e: CottaEffect, ids: (PredictedEntityId) -> AuthoritativeEntityId?): CottaEffect {
            val firstParam: Map<KParameter, Any> = mapOf(factoryInstanceParameter to companionInstance)
            val otherParams: Map<KParameter, Any?> = valueParametersToNames.mapValues { (param, name) ->
                val field = fieldsByName[name]
                val value = field?.getter?.call(e)
                if (value is PredictedEntityId) {
                    val remapped = ids(value) ?: value // TODO we actually can't just use PredictedEntityId anyway. Need some assertions or fallback to something like blank
                    logger.trace { "Remapping $value to $remapped" }
                    remapped
                } else {
                    value
                }
            }
            return factoryMethod.callBy(firstParam + otherParams)
        }
    }

    private fun getKey(obj: Component<*>): StringComponentKey {
        val kClass = obj::class
        val registeredClass = keyByClass.keys.first { it.isSuperclassOf(kClass) }
        return keyByClass[registeredClass]
            ?: throw java.lang.IllegalArgumentException("Unexpected type ${kClass.qualifiedName}")
    }

    private fun getKey(obj: CottaEffect): StringEffectKey {
        val kClass = obj::class
        val registeredClass = effectsKeyByClass.keys.first { it.isSuperclassOf(kClass) }
        return effectsKeyByClass[registeredClass]
            ?: throw java.lang.IllegalArgumentException("Unexpected type ${kClass.qualifiedName}")
    }

    private fun getInputComponentKey(kClass: KClass<out InputComponent<*>>): StringComponentKey {
        val registeredClass = inputComponentsKeyByClass.keys.first { it.isSuperclassOf(kClass) }
        return inputComponentsKeyByClass[registeredClass]
            ?: throw java.lang.IllegalArgumentException("Unexpected type ${kClass.qualifiedName}")
    }
}
