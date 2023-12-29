package com.mgtriffid.games.cotta.core.registry

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

private val logger = KotlinLogging.logger {}

/**
 * Knows about all registered component classes. Keeps component descriptors. They are needed for serialization.
 * Serializers use this registry. Something needs to build component from data and descriptor.
 */
interface ComponentsRegistry {
    companion object {
        fun getInstance(): ComponentsRegistry = ComponentsRegistryImpl()
    }

    fun <C: Component<C>> registerComponentClass(kClass: KClass<C>)
    fun <C: InputComponent<C>> registerInputComponentClass(kClass: KClass<C>)
    fun <C: CottaEffect> registerEffectClass(kClass: KClass<C>)
}

class ComponentsRegistryImpl: ComponentsRegistry {

    private val data: MutableMap<ComponentKey, ComponentSpec> = HashMap()
    private val effectSpecs: MutableMap<EffectKey, EffectSpec> = HashMap()

    private val componentRegistrationListeners = ArrayList<ComponentRegistrationListener>()
    private val inputComponentsRegistrationListeners = ArrayList<InputComponentRegistrationListener>()
    private val effectRegistrationListeners = ArrayList<EffectRegistrationListener>()

    override fun <C : Component<C>> registerComponentClass(kClass: KClass<C>) {
        logger.debug { "Registering class ${kClass.qualifiedName} as component"}
        val descriptor = createComponentSpec(kClass)
        data[descriptor.key] = descriptor
        componentRegistrationListeners.forEach { it.onComponentRegistration(kClass, descriptor) }
    }

    override fun <C : InputComponent<C>> registerInputComponentClass(kClass: KClass<C>) {
        logger.debug { "Registering class ${kClass.qualifiedName} as input component"}
        val descriptor = createComponentSpec(kClass)
        data[descriptor.key] = descriptor
        inputComponentsRegistrationListeners.forEach { it.onInputComponentRegistration(kClass, descriptor) }

    }

    override fun <C : CottaEffect> registerEffectClass(kClass: KClass<C>) {
        logger.debug { "Registering class ${kClass.qualifiedName} as effect"}
        val descriptor = createEffectSpec(kClass)
        effectSpecs[descriptor.key] = descriptor
        effectRegistrationListeners.forEach { it.onEffectRegistration(kClass, descriptor) }
    }


    private fun createComponentSpec(kClass: KClass<*>): ComponentSpec {
        logger.debug { "Creating a spec for ${kClass.qualifiedName}" }
        val classSimpleName = kClass.simpleName
            ?: throw IllegalArgumentException("Simple name absent, cannot register the class")
        val componentKey = StringComponentKey(classSimpleName)

        val fields = kClass.declaredMemberProperties.filter { it.hasAnnotation<ComponentData>() }
        logger.debug { "Found ${fields.size} fields for ${kClass.qualifiedName}:" }
        val spec: ComponentSpec = ComponentSpecImpl(
            key = componentKey, fields = fields.map { prop ->
                val mutability = if (prop is KMutableProperty1) {
                    FieldMutability.MUTABLE
                } else {
                    FieldMutability.IMMUTABLE
                }
                val type = when (prop.returnType) {
                    Byte::class.createType() -> FieldType.BYTE
                    Int::class.createType() -> FieldType.INT
                    Float::class.createType() -> FieldType.FLOAT
                    Boolean::class.createType() -> FieldType.BOOLEAN
                    Long::class.createType() -> FieldType.LONG
                    Double::class.createType() -> FieldType.DOUBLE
                    Entity.OwnedBy::class.createType() -> FieldType.OWNED_BY
                    EntityId::class.createType() -> FieldType.ENTITY_ID
                    else -> throw IllegalArgumentException("Unexpected type of field ${kClass.qualifiedName}#${prop.name}")
                }
                logger.debug { "    Field ${prop.name} of type $type" }
                FieldSpecImpl(
                    type = type,
                    mutability = mutability
                )
            }
        )

        return spec
    }

    private fun createEffectSpec(kClass: KClass<*>): EffectSpec {
        logger.debug { "Creating a spec for ${kClass.qualifiedName}" }
        val classSimpleName = kClass.simpleName
            ?: throw IllegalArgumentException("Simple name absent, cannot register the class")
        val effectKey = StringEffectKey(classSimpleName)

        val fields = kClass.declaredMemberProperties.filter { it.hasAnnotation<ComponentData>() }
        logger.debug { "Found ${fields.size} fields for ${kClass.qualifiedName}:" }
        // GROOM always immutable because they are, well, effects
        val spec: EffectSpec = EffectSpecImpl(
            key = effectKey, fields = fields.map { prop ->
                val mutability = if (prop is KMutableProperty1) {
                    FieldMutability.MUTABLE
                } else {
                    FieldMutability.IMMUTABLE
                }
                val type = when (prop.returnType) {
                    Byte::class.createType() -> FieldType.BYTE
                    Int::class.createType() -> FieldType.INT
                    Float::class.createType() -> FieldType.FLOAT
                    Boolean::class.createType() -> FieldType.BOOLEAN
                    Long::class.createType() -> FieldType.LONG
                    Double::class.createType() -> FieldType.DOUBLE
                    Entity.OwnedBy::class.createType() -> FieldType.OWNED_BY
                    EntityId::class.createType() -> FieldType.ENTITY_ID
                    else -> throw IllegalArgumentException("Unexpected type of field ${kClass.qualifiedName}#${prop.name}")
                }
                logger.debug { "    Field ${prop.name} of type $type" }
                FieldSpecImpl(
                    type = type,
                    mutability = mutability
                )
            }
        )

        return spec
    }

    fun addRegistrationListener(listener: ComponentRegistrationListener) {
        componentRegistrationListeners.add(listener)
    }

    fun addInputComponentRegistrationListener(listener: InputComponentRegistrationListener) {
        inputComponentsRegistrationListeners.add(listener)
    }

    fun addEffectRegistrationListener(listener: EffectRegistrationListener) {
        effectRegistrationListeners.add(listener)
    }
}

private data class ComponentSpecImpl(
    override val key: ComponentKey,
    override val fields: List<FieldSpec>
) : ComponentSpec

private data class FieldSpecImpl(
    override val type: FieldType,
    override val mutability: FieldMutability
) : FieldSpec

private data class EffectSpecImpl(
    override val key: EffectKey,
    override val fields: List<FieldSpec>
) : EffectSpec
