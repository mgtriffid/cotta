package com.mgtriffid.games.cotta.core.serialization.bytes

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.TraceRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesChangedEntityRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesComponentDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesCreatedEntitiesWithTracesRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesEffectRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesEntityRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesInputComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesStateRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesTraceElementRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesTraceRecipe
import com.mgtriffid.games.cotta.core.tracing.CottaTrace
import com.mgtriffid.games.cotta.core.tracing.elements.TraceElement
import jakarta.inject.Inject
import jakarta.inject.Named
import kotlin.reflect.KClass

class BytesStateSnapper @Inject constructor(
    @Named("snapper") private val kryo: Kryo,
    private val generatedComponentRegistry: ComponentRegistry
) : StateSnapper<BytesStateRecipe, BytesDeltaRecipe, BytesCreatedEntitiesWithTracesRecipe> {
    override fun snapState(entities: Entities): BytesStateRecipe {
        return BytesStateRecipe(
            entities.dynamic().map { entity -> packEntity(entity) }
        )
    }

    override fun snapDelta(prev: Entities, curr: Entities): BytesDeltaRecipe {
        return snapDelta(prev.dynamic(), curr.dynamic())
    }

    private fun snapDelta(prev: Collection<Entity>, curr: Collection<Entity>): BytesDeltaRecipe {
        val removedIds = prev.map { it.id }.toSet() - curr.map { it.id }.toSet()
        val addedEntities = curr.filter { c -> prev.none { p -> p.id == c.id } }
        val changedEntities = curr.filter { c -> prev.any { p -> p.id == c.id } }
        return BytesDeltaRecipe(
            addedEntities = addedEntities.map(::packEntity), changedEntities = changedEntities.map {
                packEntityDelta(prev = prev.find { p -> it.id == p.id }
                    ?: throw IllegalStateException("Suddenly prev entity not found but it has to be here"), curr = it)
            }, removedEntitiesIds = removedIds
        )
    }

    private fun packEntityDelta(prev: Entity, curr: Entity): BytesChangedEntityRecipe {
        val prevComponents = prev.components()
        val currComponents = curr.components()
        val removedComponents = prevComponents.map(::getKey).filter { key ->
            currComponents.none { cc -> getKey(cc) == key }
        }
        val addedComponents = currComponents.filter { cc ->
            prevComponents.none { pc -> getKey(cc) == getKey(pc) }
        }
        val changedComponents: Map<Component<*>, Component<*>> = currComponents.associateWith { cc ->
            prevComponents.find { pc -> (getKey(pc) == getKey(cc)) && (pc != cc) }
        }.filterValues { it != null } as Map<Component<*>, Component<*>>

        return BytesChangedEntityRecipe(
            entityId = curr.id,
            changedComponents = changedComponents.keys.map { cc ->
                packComponentDelta(cc)
            },
            addedComponents = addedComponents.map { packComponent(it) },
            removedComponents = removedComponents,
        )
    }

    override fun snapTrace(trace: CottaTrace): BytesTraceRecipe {
        return BytesTraceRecipe(trace.elements.map { it.toRecipe() })
    }

    // GROOM this is sooo type-unsafe because imma get rid of MapsStateSnapper and then groom
    override fun unpackTrace(trace: TraceRecipe): CottaTrace {
        trace as BytesTraceRecipe
        return CottaTrace(trace.elements.map { it.toTraceElement() })
    }

    override fun snapCreatedEntitiesWithTraces(
        createdEntities: List<Pair<CottaTrace, EntityId>>,
        associate: Map<AuthoritativeEntityId, PredictedEntityId>
    ): BytesCreatedEntitiesWithTracesRecipe {
        return BytesCreatedEntitiesWithTracesRecipe(
            traces = createdEntities.map { (trace, entityId) -> Pair(snapTrace(trace), entityId) },
            mappedPredictedIds = associate
        )
    }

    override fun unpackDeltaRecipe(entities: Entities, recipe: BytesDeltaRecipe) {
        unpackAddedEntities(entities, recipe.addedEntities)
        unpackChangedEntities(entities, recipe.changedEntities)
        recipe.removedEntitiesIds.forEach(entities::remove)
    }

    private fun unpackChangedEntities(entities: Entities, changedEntities: List<BytesChangedEntityRecipe>) {
        changedEntities.forEach { recipe ->
            val entity = entities.get(recipe.entityId) ?: return@forEach
            recipe.addedComponents.forEach {
                entity.addComponent(unpackComponentRecipe(it))
            }
            recipe.changedComponents.forEach {
                val component = unpackComponentDeltaRecipe(it)
                val clazz = generatedComponentRegistry.getDeclaredComponent(component::class)
                if (entity.hasComponent(clazz)) {
                    entity.removeComponent(clazz)
                }
                entity.addComponent(component)
            }
            recipe.removedComponents.forEach { entity.removeComponent(getComponentClassByKey(it) as KClass<out Component<*>>) }
        }
    }

    private fun unpackAddedEntities(entities: Entities, addedEntities: List<BytesEntityRecipe>) {
        addedEntities.forEach { unpackEntityRecipe(entities, it) }
    }

    private fun unpackEntityRecipe(entities: Entities, recipe: BytesEntityRecipe) {
        val entity = entities.create(recipe.entityId, recipe.ownedBy)
        recipe.components.forEach { componentRecipe -> entity.addComponent(unpackComponentRecipe(componentRecipe)) }
        recipe.inputComponents.forEach { inputComponent -> entity.addInputComponent(
            generatedComponentRegistry.getInputComponentClassByKey(inputComponent)
        ) }
    }

    private fun unpackComponentRecipe(componentRecipe: BytesComponentRecipe): Component<*> {
        val input = Input(componentRecipe.data)
        return kryo.readClassAndObject(input) as Component<*>
    }

    private fun unpackComponentDeltaRecipe(componentRecipe: BytesComponentDeltaRecipe): Component<*> {
        val input = Input(componentRecipe.data)
        return kryo.readClassAndObject(input) as Component<*>
    }

    override fun unpackStateRecipe(entities: Entities, recipe: BytesStateRecipe) {
        recipe.entities.forEach { entityRecipe -> unpackEntityRecipe(entities, entityRecipe) }
    }

    private fun packEntity(entity: Entity): BytesEntityRecipe {
        return BytesEntityRecipe(
            entityId = entity.id,
            ownedBy = entity.ownedBy,
            components = entity.components().map(::packComponent),
            inputComponents = entity.inputComponents().map(::getInputComponentKey)
        )
    }

    private fun packComponent(component: Component<*>): BytesComponentRecipe {
        return BytesComponentRecipe(
            data = kryo.run {
                val output = Output(1024)
                writeClassAndObject(output, component)
                output.toBytes()
            }
        )
    }

    private fun packComponentDelta(curr: Component<*>): BytesComponentDeltaRecipe {
        return BytesComponentDeltaRecipe(
            data = kryo.run {
                val output = Output(1024)
                writeClassAndObject(output, curr)
                output.toBytes()
            }
        )
    }

    private fun packInputComponent(inputComponent: Class<out InputComponent<*>>): BytesInputComponentRecipe {
        return BytesInputComponentRecipe(
            data = kryo.run {
                val output = Output(1024)
                writeClassAndObject(output, inputComponent)
                output.toBytes()
            }
        )
    }

    private fun getInputComponentKey(inputComponent: KClass<out InputComponent<*>>): ShortComponentKey {
        return generatedComponentRegistry.getInputComponentKey(inputComponent)
    }

    private fun getKey(component: Component<*>): ShortComponentKey {
        return generatedComponentRegistry.getKey(component::class)
    }

    private fun getComponentClassByKey(key: ShortComponentKey): KClass<out Component<*>> {
        return generatedComponentRegistry.getComponentClassByKey(key)
    }
    private fun packEffect(effect: CottaEffect): BytesEffectRecipe {
        return BytesEffectRecipe(
            data = kryo.run {
                val output = Output(1024)
                writeClassAndObject(output, effect)
                output.toBytes()
            }
        )
    }

    private fun unpackEffectRecipe(effectRecipe: BytesEffectRecipe): CottaEffect {
        val input = Input(effectRecipe.data)
        return kryo.readClassAndObject(input) as CottaEffect
    }

    private fun TraceElement.toRecipe(): BytesTraceElementRecipe {
        return when (this) {
            is TraceElement.EffectTraceElement -> {
                BytesTraceElementRecipe.BytesEffectTraceElementRecipe(packEffect(this.effect))
            }
            is TraceElement.EntityProcessingTraceElement -> TODO()
            is TraceElement.InputTraceElement -> {
                BytesTraceElementRecipe.BytesInputTraceElementRecipe(this.entityId)
            }
        }
    }

    private fun BytesTraceElementRecipe.toTraceElement(): TraceElement {
        return when (this) {
            is BytesTraceElementRecipe.BytesEffectTraceElementRecipe -> {
                TraceElement.EffectTraceElement(unpackEffectRecipe(this.effectRecipe))
            }
            is BytesTraceElementRecipe.BytesInputTraceElementRecipe -> {
                TraceElement.InputTraceElement(this.entityId)
            }

            is BytesTraceElementRecipe.BytesEntityProcessingTraceElementRecipe -> TODO()
        }
    }
}
