package com.mgtriffid.games.cotta.core.entities.arrays

import com.badlogic.gdx.utils.IntMap
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.arrays.storage.ComponentStorage
import com.mgtriffid.games.cotta.core.entities.arrays.storage.ComponentsStorage
import com.mgtriffid.games.cotta.core.entities.arrays.storage.DynamicEntitiesStorage
import com.mgtriffid.games.cotta.core.entities.arrays.storage.EntityData
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class ArraysBasedState(
    private val componentRegistry: ComponentRegistry,
    private val stateHistoryLength: Int = 64
) : StateView {
    private val tick: StateTick = StateTick(0L)
    private var idGenerator = 0
    private val entitiesStorage = DynamicEntitiesStorage(tick)
    val componentsStorage = ComponentsStorage(tick)
    private val removed = mutableListOf<EntityId>()
    private val operations = ArrayList<Operation>()

    // If entity removal should be delayed. Don't confuse with component removal!
    private var delayRemoval = 0

    override fun getEntity(id: EntityId): Entity? {
        if (!entitiesStorage.data.containsKey(id.id)) {
            return null
        }
        return getInternal(id, tick.tick)
    }

    fun atTick(tick: Long): StateView {
        return object : StateView {
            override fun getEntity(id: EntityId): Entity? {
                if (!entitiesStorage.data.containsKey(id.id)) {
                    return null
                }
                return getEntityView(id, tick)
            }

            override fun all(): Collection<Entity> {
                return entitiesStorage.data.map { e -> getEntityView(EntityId(e.key), tick) }
            }
        }
    }

    private fun getEntityView(id: EntityId, tick: Long): Entity {
        // TODO class, pooling
        return object : Entity {
            override val id: EntityId = id
            private val entityData = entitiesStorage.data.get(this.id.id)
            override val ownedBy: Entity.OwnedBy
                get() = entityData.ownedBy

            override fun <T : Component> hasComponent(clazz: KClass<T>): Boolean {
                val key = componentRegistry.getKey(clazz)
                val index = getIndex(key)
                return index != -1
            }

            override fun <T : Component> getComponent(clazz: KClass<T>): T {
                val key = componentRegistry.getKey(clazz)
                val index = getIndex(key)
                if (index == -1) {
                    throw IllegalStateException("Entity ${id.id} does not have component ${clazz.simpleName}")
                }

                return (getComponentStorage<T>(key)).get(index, tick)
            }

            private fun <T : Component> getComponentStorage(key: ShortComponentKey): ComponentStorage<T> {
                @Suppress("UNCHECKED_CAST")
                return componentsStorage.components[key.key.toInt()] as ComponentStorage<T>
            }

            private fun getIndex(
                key: ShortComponentKey
            ): Int {
                val intKey = key.key.toInt()
                val components = entityData.components
                val index = if (componentRegistry.isHistorical(key)) {
                    components.getHistorical(intKey, tick)
                } else {
                    components.get(intKey)
                }
                return index
            }

            override fun <C : Component> addComponent(component: C) {
                throw UnsupportedOperationException("Cannot add a component to a historical entity")
            }

            override fun <T : Component> removeComponent(clazz: KClass<T>) {
                throw UnsupportedOperationException("Cannot remove a component from a historical entity")
            }

            override fun components(): Collection<Component> {
                TODO("Not yet implemented")
            }
        }
    }

    private fun getInternal(id: EntityId, tick: Long) =
        object : Entity {
            override val id: EntityId = id
            private val entityData = entitiesStorage.data.get(this.id.id)
            override val ownedBy: Entity.OwnedBy
                get() = entityData.ownedBy

            override fun <T : Component> hasComponent(clazz: KClass<T>): Boolean {
                val key = componentRegistry.getKey(clazz)
                val index = getIndex(key)
                return index != -1 && !componentsStorage.components[key.key.toInt()].isMarkedRemoved(index)
            }

            private fun getIndex(key: ShortComponentKey): Int {
                val intKey = key.key.toInt()
                val components = entityData.components
                val index = if (componentRegistry.isHistorical(key)) {
                    components.getHistorical(intKey, tick)
                } else {
                    components.get(intKey)
                }
                return index
            }

            override fun <T : Component> getComponent(clazz: KClass<T>): T {
                val key = componentRegistry.getKey(clazz)
                val index = getIndex(key)
                if (index == -1) {
                    throw IllegalStateException("Entity ${this.id.id} does not have component ${clazz.simpleName}")
                }
                return getComponentStorage<T>(key).get(index)
            }

            override fun <C : Component> addComponent(component: C) {
                val key = componentRegistry.getKey(component::class)
                val intKey = key.key.toInt()
                val index = getComponentStorage<C>(key).add(component, this.id.id)
                val historical = componentRegistry.isHistorical(key)
                entityData.components.addComponent(intKey, index, historical)
            }

            override fun <T : Component> removeComponent(clazz: KClass<T>) {
                val key = componentRegistry.getKey(clazz)
                val index = entityData.components.get(key.key.toInt())
                getComponentStorage<T>(key).markRemoved(index)
                operations.add(Operation.RemoveComponent(this.id.id, key.key.toInt()))
            }

            private fun <T : Component> getComponentStorage(key: ShortComponentKey): ComponentStorage<T> {
                @Suppress("UNCHECKED_CAST")
                return componentsStorage.components[key.key.toInt()] as ComponentStorage<T>
            }

            override fun components(): Collection<Component> {
                TODO("Not yet implemented")
            }
        }

    fun removeEntity(id: EntityId) {
        if (delayRemoval > 0) {
            removed.add(id)
        } else {
            removeInternal(id)
        }
    }

    fun removeComponentInternal(entityId: Int, key: Int) {
        val componentStorage = componentsStorage.components[key]
        val index = entitiesStorage.data.get(entityId).components.get(key)
        entitiesStorage.data.get(entityId).components.removeComponent(key)
        val newEntity = componentStorage.remove(index)
        if (newEntity == -1) {
            return
        }
        entitiesStorage.data.get(newEntity).components.set(key, index)

    }

    private fun removeInternal(id: EntityId) {
        val components = entitiesStorage.data.get(id.id).components
        // TODO invent a way to not allocate an iterator
        components.all().forEach { entry ->
            val key = entry.key
            val index = entry.value
            componentsStorage.components[key].removeInternal(index)
        }
        entitiesStorage.remove(id)
    }

    fun createEntity(): Entity {
        val id = idGenerator++
        entitiesStorage.create(id)
        return getInternal(EntityId(id), tick.tick)
    }

    fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        val id = idGenerator++
        entitiesStorage.create(id, ownedBy)
        return getInternal(EntityId(id), tick.tick)
    }

    /**
     * This method's purpose is not crystal clear. There are several needs you
     * may need to query things for:
     * - To actually mutate the components you queried
     * - To mutate something different. For example, if I need to perform hit
     * scan, then I will certainly query for positions. But I will not mutate
     * them directly, I'll fire an effect there.
     *
     * If this is an internal thing then it is just a tiny bit more clear: then
     * the only purpose of this method is to supply data to an iterating system.
     *
     * If it is public - then it's a bit of a different story.
     *
     * Also, if we talk about lag comp then we need to pass immutable components
     * to the block.
     *
     */
    fun queryAndExecute(
        clazz: KClass<out Component>,
        block: (EntityId, Component) -> Unit
    ) {
        delayRemoval++
        val key = componentRegistry.getKey(clazz).key.toInt()
        val storage = componentsStorage.components[key]
        componentsStorage.delayRemoval++
        val size = storage.size
        for (i in 0 until size) {
            val entityId = storage.getEntityId(i)
            block(EntityId(entityId), storage.get(i))
        }
        flushRemovals()
    }

    private fun flushRemovals() {
        if (--delayRemoval > 0) return
        val iterator = operations.iterator()
        while (iterator.hasNext()) {
            when (val operation = iterator.next()) {
                is Operation.RemoveComponent -> {
                    removeComponentInternal(operation.entity, operation.key)
                    iterator.remove()
                }
            }
        }
        for (id in removed) {
            removeInternal(id)
        }
        removed.clear()
    }

    // TODO uniform parameter names
    // TODO proper usage of generics
    fun queryAndExecute(
        clazz1: KClass<out Component>,
        clazz2: KClass<out Component>,
        block: (EntityId, Component, Component) -> Unit
    ) {
        val key1 = componentRegistry.getKey(clazz1).key.toInt()
        val key2 = componentRegistry.getKey(clazz2).key.toInt()
        val storage1 = componentsStorage.components[key1]
        val storage2 = componentsStorage.components[key2]
        val storage: ComponentStorage<*>
        val minStorageKey: Int
        val size1 = storage1.size
        val size2 = storage2.size
        if (size2 < size1) {
            storage = storage2
            minStorageKey = key2
        } else {
            storage = storage1
            minStorageKey = key1
        }
        for (i in 0 until storage.size) {
            val entityId = storage.getEntityId(i)
            // use only those Entities which have both components:
            val entityComponents = entitiesStorage.data.get(entityId).components
            val c1index =
                if (minStorageKey == key1) i else entityComponents.get(key1)
            if (c1index == -1) {
                continue
            }
            val c2index =
                if (minStorageKey == key2) i else entityComponents.get(key2)
            if (c2index == -1) {
                continue
            }
            val c1 = storage1.get(c1index)
            val c2 = storage2.get(c2index)
            block(EntityId(entityId), c1, c2)
        }
        flushRemovals()
    }

    fun advance() {
        tick.tick++
        entitiesStorage.advance()
        componentsStorage.advance()
    }

    override fun all() : Collection<Entity> {
        return entitiesStorage.data.entries().map { it: IntMap.Entry<EntityData> -> getInternal(
            EntityId(it.key), tick.tick
        )}
    }

    private sealed interface Operation {
        data class RemoveComponent(val entity: Int, val key: Int) : Operation
    }
}
