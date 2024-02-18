package com.mgtriffid.games.cotta.core.registry.impl

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ComponentKey
import com.mgtriffid.games.cotta.core.registry.ComponentSpec
import com.mgtriffid.games.cotta.core.registry.EffectKey
import com.mgtriffid.games.cotta.core.registry.EffectSpec
import com.mgtriffid.games.cotta.core.registry.FieldMutability
import com.mgtriffid.games.cotta.core.registry.FieldSpec
import com.mgtriffid.games.cotta.core.registry.FieldType
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.registry.StringEffectKey
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

private val logger = KotlinLogging.logger {}

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

fun createComponentSpec(kClass: KClass<*>): ComponentSpec {
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

fun createEffectSpec(kClass: KClass<*>): EffectSpec {
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
