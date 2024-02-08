package com.mgtriffid.games.cotta.core.serialization.maps

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.EffectData
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.*
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.maps.dto.EntityIdDto
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.*
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement
import mu.KotlinLogging
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.kotlinFunction

private val logger = KotlinLogging.logger {}

const val SYSTEM_PLAYER_ID = -1

class MapsStateSnapper : StateSnapper<MapsStateRecipe, MapsDeltaRecipe> {
    private val snappers = HashMap<ComponentKey, ComponentSnapper<*>>()
    private val deltaSnappers = HashMap<ComponentKey, ComponentDeltaSnapper<*>>()

    private val keyByClass = HashMap<KClass<*>, StringComponentKey>()
    private val inputComponentsKeyByClass = HashMap<KClass<*>, StringComponentKey>()
    private val classByKey = HashMap<StringComponentKey, KClass<Component<*>>>()
    private val inputComponentsClassByKey = HashMap<StringComponentKey, KClass<InputComponent<*>>>()
    // TODO delete
    private val factoryMethodsByClass = HashMap<ComponentKey, KCallable<*>>()
    // GROOM extract out
    private val effectSnappers = HashMap<EffectKey, EffectSnapper<*>>()
    private val effectsKeyByClass = HashMap<KClass<*>, StringEffectKey>()
    private val effectsClassByKey = HashMap<StringEffectKey, KClass<*>>()

    fun <T : Component<T>> registerComponent(kClass: KClass<T>, spec: ComponentSpec) {
        logger.debug { "Registering component ${kClass.qualifiedName}, spec has key of ${spec.key}" }
        keyByClass[kClass] = spec.key as StringComponentKey // hack, the fact that maps and componentregistry are connected leaks in here but okay
        registerSnapper(kClass, spec)
        registerDeltaSnapper(kClass, spec)
        classByKey[spec.key as StringComponentKey] = kClass as KClass<Component<*>>
    }

    fun <T : InputComponent<T>> registerInputComponent(kClass: KClass<T>, spec: ComponentSpec) {
        logger.debug { "Registering input component ${kClass.qualifiedName}" }
        inputComponentsKeyByClass[kClass] = spec.key as StringComponentKey // hack, the fact that maps and componentregistry are connected leaks in here but okay
        inputComponentsClassByKey[spec.key as StringComponentKey] = kClass as KClass<InputComponent<*>>
    }

    fun <T : CottaEffect> registerEffect(kClass: KClass<T>, spec: EffectSpec) {
        logger.debug { "Registering effect ${kClass.qualifiedName}" }
        effectsKeyByClass[kClass] = spec.key as StringEffectKey // hack, the fact that maps and componentregistry are connected leaks in here but okay
        registerEffectSnapper(kClass, spec)
        effectsClassByKey[spec.key as StringEffectKey] = kClass as KClass<*>
    }

    private fun <C : Component<C>> registerSnapper(kClass: KClass<C>, spec: ComponentSpec) {
        val fields = kClass.declaredMemberProperties.filter { it.hasAnnotation<ComponentData>() }
        val factoryMethod: KCallable<C> = kClass.java.classLoader.loadClass("${kClass.qualifiedName}ImplKt").declaredMethods.find { it.name == "create${kClass.simpleName}" }!!.kotlinFunction as KCallable<C>
        val fieldsByName = fields.associateBy { it.name }

        val factoryParameters = factoryMethod.parameters
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

        snappers[spec.key] = ComponentSnapper(
            key = spec.key as StringComponentKey,
            factoryMethod = factoryMethod,
            valueParametersToNames = valueParametersToNames,
            fieldsByName = fieldsByName
        )
    }

    fun <C: CottaEffect> registerEffectSnapper(kClass: KClass<C>, spec: EffectSpec) {
        val companion = kClass.companionObject
            ?: throw IllegalArgumentException("${kClass.qualifiedName} does not have a companion object")
        val companionInstance =
            kClass.companionObjectInstance ?: throw IllegalArgumentException("Could not find companion instance")
        val factoryMethod: KCallable<C> = (companion.members.find { it.name == "create" }
            ?: throw IllegalArgumentException("${kClass.qualifiedName} has no 'create' method")) as KCallable<C>
        val fields = kClass.declaredMemberProperties.filter { it.hasAnnotation<EffectData>() }

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

        effectSnappers[spec.key] = EffectSnapper(
            key = spec.key as StringEffectKey,
            factoryMethod = factoryMethod,
            factoryInstanceParameter = factoryInstanceParameter,
            companionInstance = companionInstance,
            valueParametersToNames = valueParametersToNames,
            fieldsByName = fieldsByName
        )
    }

    private fun <C : Component<C>> registerDeltaSnapper(kClass: KClass<C>, spec: ComponentSpec) {
        val fields = kClass.declaredMemberProperties.filter {
            it.hasAnnotation<ComponentData>()
        }.filterIsInstance<KMutableProperty1<C, *>>()
        val fieldsByName = fields.associateBy { it.name }
        deltaSnappers[spec.key] = ComponentDeltaSnapper(
            key = spec.key as StringComponentKey, fieldsByName = fieldsByName
        )
    }

    private inner class ComponentSnapper<C : Component<C>>(
        val key: StringComponentKey,
        val factoryMethod: KCallable<C>,
        val valueParametersToNames: Map<KParameter, String?>,
        val fieldsByName: Map<String, KProperty1<C, *>>
    ) {
        private val valueParameters: Collection<KParameter> = valueParametersToNames.keys

        fun packComponent(obj: C): MapComponentRecipe {
            return MapComponentRecipe(componentKey = key, data = fieldsByName.mapValues { (_, field) ->
                try {
                    serializeProperty(field.get(obj), field) ?: throw IllegalStateException("Nullable fields are not allowed")
                } catch (e: Exception) {
                    logger.error { "Field: ${field.name}, object: $obj could not be packed" }
                    throw e
                }
            })
        }

        fun unpackComponent(recipe: MapComponentRecipe): C {
            val otherParams: Map<KParameter, Any?> = valueParameters.associateWith { p: KParameter ->
                val propertyName = valueParametersToNames[p]
                deserializeProperty(
                    value = recipe.data[propertyName],
                    prop = fieldsByName[propertyName]
                        ?: throw IllegalArgumentException("Property $propertyName not found")
                )
            }
            return factoryMethod.callBy(
                otherParams
            )
        }
    }

    private inner class ComponentDeltaSnapper<C : Component<C>>(
        private val key: StringComponentKey,
        private val fieldsByName: Map<String, KMutableProperty1<C, *>>
    ) {
        fun packDelta(prev: C, curr: C): MapComponentDeltaRecipe {
            return MapComponentDeltaRecipe(
                componentKey = key, data = fieldsByName.mapNotNull { (name, field) ->
                    val v0 = field.get(prev)
                    val v1 = field.get(curr)!!
                    if (v0 != v1) {
                        Pair(name, v1)
                    } else {
                        null
                    }
                }.toMap()
            )
        }

        fun apply(delta: MapComponentDeltaRecipe, target: Any) {
            delta.data.forEach { (name, value) ->
                (fieldsByName[name]!! as KMutableProperty1<Any, Any?>).set(target, value)
            }
        }
    }

    private inner class EffectSnapper<E : CottaEffect>(
        private val key: StringEffectKey,
        private val factoryMethod: KCallable<E>,
        private val factoryInstanceParameter: KParameter,
        private val companionInstance: Any,
        private val valueParametersToNames: Map<KParameter, String?>,
        private val fieldsByName: Map<String, KProperty1<E, *>>
    ) {
        private val valueParameters: Collection<KParameter> = valueParametersToNames.keys

        fun packEffect(obj: E): MapEffectRecipe {
            return MapEffectRecipe(effectKey = key, data = fieldsByName.mapValues { (_, field) ->
                try {
                    serializeProperty(field.get(obj), field) ?: throw IllegalStateException("Nullable fields are not allowed")
                } catch (e: Exception) {
                    logger.error { "Field: ${field.name}, object: $obj could not be packed" }
                    throw e
                }
            })
        }

        fun unpackEffect(recipe: MapEffectRecipe): CottaEffect {
            val firstParam: Map<KParameter, Any> = mapOf(factoryInstanceParameter to companionInstance)
            val otherParams: Map<KParameter, Any?> = valueParameters.associateWith { p: KParameter ->
                val propertyName = valueParametersToNames[p]
                deserializeProperty(
                    value = recipe.data[propertyName],
                    prop = fieldsByName[propertyName]
                        ?: throw IllegalArgumentException("Property $propertyName not found")
                )
            }
            return factoryMethod.callBy(
                firstParam + otherParams
            )
        }
    }

    private fun getKey(obj: Component<*>): StringComponentKey {
        val kClass = obj::class
        val registeredClass = keyByClass.keys.first { it.isSuperclassOf(kClass) }
        return keyByClass[registeredClass] ?: throw java.lang.IllegalArgumentException("Unexpected type ${kClass.qualifiedName}")
    }

    private fun getKey(obj: CottaEffect): StringEffectKey {
        val kClass = obj::class
        val registeredClass = effectsKeyByClass.keys.first { it.isSuperclassOf(kClass) }
        return effectsKeyByClass[registeredClass] ?: throw java.lang.IllegalArgumentException("Unexpected type ${kClass.qualifiedName}")
    }

    private fun getInputComponentKey(kClass: KClass<out InputComponent<*>>): StringComponentKey {
        val registeredClass = inputComponentsKeyByClass.keys.first { it.isSuperclassOf(kClass) }
        return inputComponentsKeyByClass[registeredClass] ?: throw java.lang.IllegalArgumentException("Unexpected type ${kClass.qualifiedName}")
    }

    override fun snapState(entities: Entities): MapsStateRecipe {
        return MapsStateRecipe(entities.dynamic().map { e ->
            packEntity(e)
        })
    }

    override fun snapTrace(trace: CottaTrace): MapsTraceRecipe {
        return MapsTraceRecipe(trace.elements.map { it.toRecipe() })
    }

    override fun unpackTrace(trace: MapsTraceRecipe): CottaTrace {
        return CottaTrace(trace.elements.map { it.toTraceElement() })
    }

    private fun MapsTraceElementRecipe.toTraceElement(): TraceElement {
        return when (this) {
            is MapsTraceElementRecipe.MapsEffectTraceElementRecipe -> {
                TraceElement.EffectTraceElement(unpackEffectRecipe(this.effectRecipe))
            }
            is MapsTraceElementRecipe.MapsInputTraceElementRecipe -> {
                TraceElement.InputTraceElement(this.entityId)
            }

            is MapsTraceElementRecipe.MapsEntityProcessingTraceElementRecipe -> TODO()
        }
    }

    private fun TraceElement.toRecipe(): MapsTraceElementRecipe {
        return when (this) {
            is TraceElement.EffectTraceElement -> {
                MapsTraceElementRecipe.MapsEffectTraceElementRecipe(packEffect<CottaEffect>(this.effect))
            }
            is TraceElement.EntityProcessingTraceElement -> TODO()
            is TraceElement.InputTraceElement -> {
                MapsTraceElementRecipe.MapsInputTraceElementRecipe(this.entityId)
            }
        }
    }

    private fun packEntity(e: Entity) =
        MapsEntityRecipe(
            entityId = e.id,
            ownedBy = e.ownedBy,
            components = e.components().map { packComponent(it) },
            inputComponents = e.inputComponents().map { getInputComponentKey(it) }
        )

    // TODO shit wtf is this mess with unsafe casts
    private fun <C : Component<C>> packComponent(obj: Any): MapComponentRecipe {
        obj as C
        return (snappers[getKey(obj)] as ComponentSnapper<C>).packComponent(obj)
    }

    fun <C : CottaEffect> packEffect(obj: Any): MapEffectRecipe {
        obj as C
        return (effectSnappers[getKey(obj)] as EffectSnapper<C>).packEffect(obj)
    }
    fun unpackEffectRecipe(recipe: MapEffectRecipe): CottaEffect {
        return effectSnappers[recipe.effectKey]?.unpackEffect(recipe)
            ?: throw java.lang.IllegalArgumentException("Effect Snapper not found for effect ${recipe.effectKey}")
    }

    override fun snapDelta(prev: Entities, curr: Entities): MapsDeltaRecipe {
        return snapDelta(prev.dynamic(), curr.dynamic())
    }

    override fun unpackStateRecipe(entities: Entities, recipe: MapsStateRecipe) {
        recipe.entities.forEach { entityRecipe -> unpackEntityRecipe(entities, entityRecipe) }
    }

    private fun unpackEntityRecipe(entities: Entities, recipe: MapsEntityRecipe) {
        val entity = entities.create(recipe.entityId, recipe.ownedBy)
        recipe.components.forEach { componentRecipe -> entity.addComponent(unpackComponentRecipe(componentRecipe)) }
        recipe.inputComponents.forEach { inputComponent -> entity.addInputComponent(inputComponentsClassByKey[inputComponent] as KClass<out InputComponent<*>>) }
    }

    private fun unpackComponentRecipe(recipe: MapComponentRecipe): Component<*> {
        return snappers[recipe.componentKey]?.unpackComponent(recipe)
            // mb not the best idea, malformed data should not break client
            ?: throw IllegalArgumentException("Component Snapper not found for component ${recipe.componentKey}")
    }

    private fun unpackComponentDeltaRecipe(component: Any, recipe: MapComponentDeltaRecipe) {
        (deltaSnappers[recipe.componentKey] ?: throw IllegalArgumentException("Delta Snapper not found")).apply(
            recipe,
            component
        )
    }

    override fun unpackDeltaRecipe(entities: Entities, recipe: MapsDeltaRecipe) {
        unpackAddedEntities(entities, recipe.addedEntities)
        unpackChangedEntities(entities, recipe.changedEntities)
        recipe.removedEntitiesIds.forEach { entities.remove(it) }
    }

    private fun unpackAddedEntities(entities: Entities, addedEntities: List<MapsEntityRecipe>) {
        addedEntities.forEach { unpackEntityRecipe(entities, it) }
    }

    private fun unpackChangedEntities(entities: Entities, changedEntities: List<MapsChangedEntityRecipe>) {
        changedEntities.forEach { recipe ->
            val entity = entities.get(recipe.entityId) ?: return@forEach
            recipe.addedComponents.forEach {
                entity.addComponent(unpackComponentRecipe(it))
            }
            recipe.changedComponents.forEach {
                // cast and fucking pray
                val kClass = classByKey[it.componentKey]
                val component = entity.getComponent(kClass as KClass<out Component<*>>)
                logger.debug { "Unpacked changed component ${it.data}" }
                unpackComponentDeltaRecipe(component as Any, it)
            }
            recipe.removedComponents.forEach { entity.removeComponent(classByKey[it] as KClass<out Component<*>>) }
        }
    }

    private fun snapDelta(prev: Collection<Entity>, curr: Collection<Entity>): MapsDeltaRecipe {
        val removedIds = prev.map { it.id }.toSet() - curr.map { it.id }.toSet()
        val addedEntities = curr.filter { c -> prev.none { p -> p.id == c.id } }
        val changedEntities = curr.filter { c -> prev.any { p -> p.id == c.id } }
        return MapsDeltaRecipe(
            addedEntities = addedEntities.map(::packEntity), changedEntities = changedEntities.map {
                packEntityDelta(prev = prev.find { p -> it.id == p.id }
                    ?: throw IllegalStateException("Suddenly prev entity not found but it has to be here"), curr = it)
            }, removedEntitiesIds = removedIds
        )
    }

    private fun packEntityDelta(prev: Entity, curr: Entity): MapsChangedEntityRecipe {
        val prevComponents = prev.components()
        val currComponents = curr.components()
        val removedComponents = prevComponents.map(::getKey).filter { key ->
            currComponents.none { cc -> getKey(cc) == key }
        }
        val adderComponents = currComponents.filter { cc ->
            prevComponents.none { pc -> getKey(cc) == getKey(pc) }
        }
        val changedComponents: Map<Component<*>, Component<*>> = currComponents.associateWith { cc ->
            prevComponents.find { pc -> (getKey(pc) == getKey(cc)) && (pc != cc) }
        }.filterValues { it != null } as Map<Component<*>, Component<*>>

        return MapsChangedEntityRecipe(
            entityId = curr.id,
            changedComponents = changedComponents.map { (cc, pc) ->
                packComponentDelta(pc, cc)
            },
            addedComponents = adderComponents.map { packComponent(it) },
            removedComponents = removedComponents,
        )
    }

    private fun <C: Component<C>> packComponentDelta(c0: Component<*>, c1: Component<*>): MapComponentDeltaRecipe {
        val prev = c0 as C // I apologize
        val curr = c1 as C
        logger.debug { "Packing component delta: prev = $prev, curr = $curr" }
        return (deltaSnappers[getKey(curr)]!! as ComponentDeltaSnapper<C>).packDelta(prev, curr)
    }
}

private fun deserializeProperty(value: Any?, prop: KProperty1<*, *>?, ): Any? {
    return when (prop?.returnType) {
        Byte::class.createType() -> value
        Int::class.createType() -> value
        Float::class.createType() -> value
        Boolean::class.createType() -> value
        Long::class.createType() -> value
        Double::class.createType() -> value
        Entity.OwnedBy::class.createType() -> when (val id = value as Int) {
            SYSTEM_PLAYER_ID -> Entity.OwnedBy.System
            else -> Entity.OwnedBy.Player(PlayerId(id))
        }
        EntityId::class.createType() -> (value as EntityIdDto).toEntityId()
        else -> throw IllegalArgumentException("Unexpected type of field #${prop?.name}")
    }
}

private fun serializeProperty(value: Any?, prop: KProperty1<*, *>?, ): Any? {
    return when (prop?.returnType) {
        Byte::class.createType() -> value
        Int::class.createType() -> value
        Float::class.createType() -> value
        Boolean::class.createType() -> value
        Long::class.createType() -> value
        Double::class.createType() -> value
        Entity.OwnedBy::class.createType() -> when (val id = value as Entity.OwnedBy) {
            Entity.OwnedBy.System -> SYSTEM_PLAYER_ID
            is Entity.OwnedBy.Player -> id.playerId.id
        }
        EntityId::class.createType() -> (value as EntityId).toDto()
        else -> throw IllegalArgumentException("Unexpected type of field #${prop?.name}")
    }
}
