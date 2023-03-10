package com.mgtriffid.games.cotta.core.serialization.impl

import com.mgtriffid.games.cotta.ComponentData
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.registry.ComponentKey
import com.mgtriffid.games.cotta.core.registry.ComponentSpec
import com.mgtriffid.games.cotta.core.registry.StringComponentKey
import com.mgtriffid.games.cotta.core.serialization.ComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import kotlin.IllegalStateException
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

class MapsStateSnapperImpl : StateSnapper<MapsStateRecipe, MapsDeltaRecipe> {
    private val snappers = HashMap<ComponentKey, ComponentSnapper<*>>()
    private val deltaSnappers = HashMap<ComponentKey, ComponentDeltaSnapper<*>>()

    private val keyByClass = HashMap<KClass<*>, StringComponentKey>()
    private val classByKey = HashMap<ComponentKey, KClass<*>>()
    private val factoryMethodsByClass = HashMap<ComponentKey, KCallable<*>>()

    fun <T : Component<T>> registerComponent(kClass: KClass<T>, spec: ComponentSpec) {
        keyByClass[kClass] = spec.key as StringComponentKey // hack, the fact that maps8 and componentregistry are connected leaks in here but okay
        registerSnapper(kClass, spec)
        registerDeltaSnapper(kClass, spec)
    }

    private fun <C : Component<C>> registerSnapper(kClass: KClass<C>, spec: ComponentSpec) {
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

        snappers[spec.key] = ComponentSnapper(
            key = spec.key as StringComponentKey,
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
        val factoryInstanceParameter: KParameter,
        val companionInstance: Any,
        val valueParametersToNames: Map<KParameter, String?>,
        val fieldsByName: Map<String, KProperty1<C, *>>
    ) {
        private val valueParameters: Collection<KParameter> = valueParametersToNames.keys

        fun packComponent(obj: C): MapComponentRecipe<C> {
            return MapComponentRecipe(componentKey = key, data = fieldsByName.mapValues { (_, field) ->
                field.get(obj) ?: throw IllegalStateException("Nullable fields are not allowed")
            })
        }

        fun unpackComponent(recipe: MapComponentRecipe<C>): C {
            val firstParam: Map<KParameter, Any> = mapOf(factoryInstanceParameter to companionInstance)
            val otherParams: Map<KParameter, Any?> = valueParameters.associateWith { p: KParameter ->
                recipe.data[valueParametersToNames[p]]
            }
            return factoryMethod.callBy(
                firstParam + otherParams
            )
        }
    }

    private inner class ComponentDeltaSnapper<C : Component<C>>(
        private val key: StringComponentKey,
        private val fieldsByName: Map<String, KMutableProperty1<C, *>>
    ) {
        fun packDelta(prev: C, curr: C): MapComponentDeltaRecipe<C> {
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

        private fun apply(delta: MapComponentDeltaRecipe<C>, target: C) {
            delta.data.forEach { (name, value) ->
                (fieldsByName[name]!! as KMutableProperty1<C, Any?>).set(target, value)
            }
        }
    }

    private fun getKey(obj: Component<*>): StringComponentKey {
        val kClass = obj::class
        return keyByClass[kClass] ?: throw java.lang.IllegalArgumentException("Unexpected type ${kClass.qualifiedName}")
    }

    override fun snapState(entities: Entities): MapsStateRecipe {
        return MapsStateRecipe(entities.all().map { e ->
            packEntity(e)
        })
    }

    private fun packEntity(e: Entity) =
        MapsEntityRecipe(entityId = e.id, components = e.components().map { packComponent(it) })

    // TODO shit wtf is this mess with unsafe casts
    private fun <C : Component<C>> packComponent(obj: Any): MapComponentRecipe<C> {
        obj as C
        return (snappers[getKey(obj)] as ComponentSnapper<C>).packComponent(obj)
    }

    override fun snapDelta(prev: Entities, curr: Entities): MapsDeltaRecipe {
        return snapDelta(prev.all(), curr.all())
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
            prevComponents.find { pc -> getKey(pc) == getKey(cc) }
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

    private fun <C: Component<C>> packComponentDelta(c1: Component<*>, c0: Component<*>): MapComponentDeltaRecipe<C> {
        val prev = c0 as C // I apologize
        val curr = c1 as C
        return (deltaSnappers[getKey(curr)]!! as ComponentDeltaSnapper<C>).packDelta(prev, curr)
    }
}
