package com.mgtriffid.games.cotta.core.serialization.bytes

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.impl.EntitiesInternal
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import com.mgtriffid.games.cotta.core.serialization.StateSnapper
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesChangedEntityRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesComponentDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesComponentRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesEntityRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesPlayersDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesStateRecipe
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class BytesStateSnapper @Inject constructor(
    @Named("snapper") private val kryo: Kryo,
    private val generatedComponentRegistry: ComponentRegistry
) : StateSnapper<
    BytesStateRecipe,
    BytesDeltaRecipe,
    BytesPlayersDeltaRecipe
    > {

    // TODO stupid naming
    override fun snapState(
        entities: EntitiesInternal,
    ): BytesStateRecipe {
        return BytesStateRecipe(
            entities.dynamic().map { entity -> packEntity(entity) },
        )
    }

    override fun snapDelta(prev: EntitiesInternal, curr: EntitiesInternal): BytesDeltaRecipe {
        return snapDelta(prev.dynamic(), curr.dynamic(), curr.currentId())
    }

    private fun snapDelta(
        prev: Collection<Entity>,
        curr: Collection<Entity>,
        idSequence: Int
    ): BytesDeltaRecipe {
        val removedIds = prev.map { it.id }.toSet() - curr.map { it.id }.toSet()
        val addedEntities = curr.filter { c -> prev.none { p -> p.id == c.id } }
        val changedEntities =
            curr.filter { c -> prev.any { p -> p.id == c.id } }
        return BytesDeltaRecipe(
            addedEntities = addedEntities.map(::packEntity),
            changedEntities = changedEntities.map {
                packEntityDelta(prev = prev.find { p -> it.id == p.id }
                    ?: throw IllegalStateException("Suddenly prev entity not found but it has to be here"),
                    curr = it)
            },
            removedEntitiesIds = removedIds,
            idSequence = idSequence
        )
    }

    private fun packEntityDelta(
        prev: Entity,
        curr: Entity
    ): BytesChangedEntityRecipe {
        val prevComponents = prev.components()
        val currComponents = curr.components()
        val removedComponents = prevComponents.map(::getKey).filter { key ->
            currComponents.none { cc -> getKey(cc) == key }
        }
        val addedComponents = currComponents.filter { cc ->
            prevComponents.none { pc -> getKey(cc) == getKey(pc) }
        }
        val changedComponents: Map<Component<*>, Component<*>> =
            currComponents.associateWith { cc ->
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

    override fun unpackDeltaRecipe(
        entities: EntitiesInternal,
        recipe: BytesDeltaRecipe
    ) {
        unpackAddedEntities(entities, recipe.addedEntities)
        unpackChangedEntities(entities, recipe.changedEntities)
        recipe.removedEntitiesIds.forEach(entities::remove)
        entities.setIdGenerator(recipe.idSequence)
        logger.debug { "Id coming from Server with delta is ${recipe.idSequence}" }
    }

    override fun snapPlayersDelta(
        addedPlayers: List<PlayerId>
    ): BytesPlayersDeltaRecipe {
        return BytesPlayersDeltaRecipe(
            addedPlayers = addedPlayers
        )
    }

    override fun unpackPlayersDeltaRecipe(recipe: BytesPlayersDeltaRecipe): List<PlayerId> {
        return recipe.addedPlayers
    }

    private fun unpackChangedEntities(
        entities: Entities,
        changedEntities: List<BytesChangedEntityRecipe>
    ) {
        changedEntities.forEach { recipe ->
            val entity = entities.get(recipe.entityId) ?: return@forEach
            recipe.addedComponents.forEach {
                entity.addComponent(unpackComponentRecipe(it))
            }
            recipe.changedComponents.forEach {
                val component = unpackComponentDeltaRecipe(it)
                val clazz =
                    generatedComponentRegistry.getDeclaredComponent(component::class)
                if (entity.hasComponent(clazz)) {
                    entity.removeComponent(clazz)
                }
                entity.addComponent(component)
            }
            recipe.removedComponents.forEach {
                entity.removeComponent(
                    getComponentClassByKey(it)
                )
            }
        }
    }

    private fun unpackAddedEntities(
        entities: EntitiesInternal,
        addedEntities: List<BytesEntityRecipe>
    ) {
        addedEntities.forEach { unpackEntityRecipe(entities, it) }
    }

    private fun unpackEntityRecipe(
        entities: EntitiesInternal,
        recipe: BytesEntityRecipe
    ) {
        val entity = entities.create(recipe.entityId, recipe.ownedBy)
        recipe.components.forEach { componentRecipe ->
            entity.addComponent(
                unpackComponentRecipe(componentRecipe)
            )
        }
    }

    private fun unpackComponentRecipe(componentRecipe: BytesComponentRecipe): Component<*> {
        val input = Input(componentRecipe.data)
        return kryo.readClassAndObject(input) as Component<*>
    }

    private fun unpackComponentDeltaRecipe(componentRecipe: BytesComponentDeltaRecipe): Component<*> {
        val input = Input(componentRecipe.data)
        return kryo.readClassAndObject(input) as Component<*>
    }

    override fun unpackStateRecipe(
        entities: EntitiesInternal,
        recipe: BytesStateRecipe
    ) {
        recipe.entities.forEach { entityRecipe ->
            unpackEntityRecipe(
                entities,
                entityRecipe
            )
        }
    }

    private fun packEntity(entity: Entity): BytesEntityRecipe {
        return BytesEntityRecipe(
            entityId = entity.id,
            ownedBy = entity.ownedBy,
            components = entity.components().map(::packComponent),
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

    private fun getKey(component: Component<*>): ShortComponentKey {
        return generatedComponentRegistry.getKey(component::class)
    }

    private fun getComponentClassByKey(key: ShortComponentKey): KClass<out Component<*>> {
        return generatedComponentRegistry.getComponentClassByKey(key)
    }
}
