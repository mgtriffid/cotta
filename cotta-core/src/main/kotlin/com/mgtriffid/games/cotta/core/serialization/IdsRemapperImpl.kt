package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.registry.ComponentKey
import com.mgtriffid.games.cotta.core.registry.ComponentSpec
import com.mgtriffid.games.cotta.core.registry.EffectKey
import com.mgtriffid.games.cotta.core.registry.EffectSpec
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.registry.StringEffectKey
import mu.KotlinLogging
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.kotlinFunction

class IdsRemapperImpl : IdsRemapper {

    private var componentsRemappers = mutableMapOf<ComponentKey, ComponentRemapper>()
    private var inputComponentsRemappers = mutableMapOf<ComponentKey, InputComponentRemapper>()
    private var effectsRemappers = mutableMapOf<EffectKey, EffectRemapper>()

    private val keyByClass = HashMap<KClass<*>, StringComponentKey>()

    private val inputComponentsKeyByClass = HashMap<KClass<*>, ComponentKey>()
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
        val factoryMethod: KCallable<C> = kClass.java.classLoader.loadClass("${kClass.qualifiedName}ImplKt").declaredMethods.find { it.name == "create${kClass.simpleName}" }!!.kotlinFunction as KCallable<C>
        val fields = kClass.declaredMemberProperties

        val fieldsByName = fields.associateBy { it.name }

        val factoryParameters = factoryMethod.parameters
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
                factoryMethod,
                valueParametersToNames,
                fieldsByName,
            )
        }
    }

    fun <T : InputComponent<T>> registerInputComponent(kClass: KClass<T>, spec: ComponentSpec) {
        logger.debug { "Registering input component ${kClass.qualifiedName}, spec has key of ${spec.key}" }
        inputComponentsKeyByClass[kClass] = spec.key as StringComponentKey // hack, the fact that maps and componentregistry are connected leaks in here but okay

        registerInputComponentRemapper(kClass, spec)
    }

    fun <C: InputComponent<C>> registerInputComponentRemapper(kClass: KClass<C>, spec: ComponentSpec) {
        val factoryMethod: KCallable<C> = kClass.java.classLoader.loadClass("${kClass.qualifiedName}ImplKt").declaredMethods.find { it.name == "create${kClass.simpleName}" }?.kotlinFunction as? KCallable<C>
            ?: throw IllegalArgumentException("${kClass.qualifiedName} has no 'create' method")
        val fields = kClass.declaredMemberProperties

        val fieldsByName = fields.associateBy { it.name }

        val factoryParameters = factoryMethod.parameters
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
                factoryMethod,
                valueParametersToNames,
                fieldsByName,
            )
        }
    }

    fun <T : CottaEffect> registerEffect(kClass: KClass<T>, spec: EffectSpec) {
        logger.debug { "Registering effect ${kClass.qualifiedName}" }
        effectsKeyByClass[kClass] = spec.key as StringEffectKey // hack, the fact that maps and componentregistry are connected leaks in here but okay

        registerEffectMapper(kClass, spec) // hack, the fact that maps and componentregistry are connected leaks in here but okay
    }

    private fun <T : CottaEffect> registerEffectMapper(kClass: KClass<T>, spec: EffectSpec) {
        val factoryMethod: KCallable<T> = kClass.java.classLoader.loadClass("${kClass.qualifiedName}ImplKt").declaredMethods.find { it.name == "create${kClass.simpleName}" }!!.kotlinFunction as KCallable<T>
        val fields = kClass.declaredMemberProperties

        val fieldsByName = fields.associateBy { it.name }

        val factoryParameters = factoryMethod.parameters
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
                factoryMethod,
                valueParametersToNames,
                fieldsByName,
            )
        }
    }

    private interface ComponentRemapper {
        fun remap(c: Component<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): Component<*>
    }

    private class ComponentRemapperImpl<C : Component<C>>(
        private val factoryMethod: KCallable<C>,
        private val valueParametersToNames: Map<KParameter, String?>,
        private val fieldsByName: Map<String, KProperty1<C, *>>,
    ) : ComponentRemapper {

        override fun remap(c: Component<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): Component<*> {
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
            return factoryMethod.callBy(otherParams)
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
        private val factoryMethod: KCallable<C>,
        private val valueParametersToNames: Map<KParameter, String?>,
        private val fieldsByName: Map<String, KProperty1<C, *>>,
    ) : InputComponentRemapper {

        override fun remap(ic: InputComponent<*>, ids: (PredictedEntityId) -> AuthoritativeEntityId?): InputComponent<*> {
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
            return factoryMethod.callBy(otherParams)
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
        private val factoryMethod: KCallable<CottaEffect>,
        private val valueParametersToNames: Map<KParameter, String?>,
        private val fieldsByName: Map<String, KProperty1<C, *>>,
    ) : EffectRemapper {
        override fun remap(e: CottaEffect, ids: (PredictedEntityId) -> AuthoritativeEntityId?): CottaEffect {
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
            return factoryMethod.callBy(otherParams)
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

    private fun getInputComponentKey(kClass: KClass<out InputComponent<*>>): ComponentKey {
        val registeredClass = inputComponentsKeyByClass.keys.first { it.isSuperclassOf(kClass) }
        return inputComponentsKeyByClass[registeredClass]
            ?: throw java.lang.IllegalArgumentException("Unexpected type ${kClass.qualifiedName}")
    }
}

private val logger = KotlinLogging.logger {}
