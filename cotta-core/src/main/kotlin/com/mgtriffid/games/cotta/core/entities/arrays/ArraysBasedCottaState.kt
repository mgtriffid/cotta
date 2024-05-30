package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class ArraysBasedState(
    private val componentRegistry: ComponentRegistry,
    private val stateHistoryLength: Int = 64
): StateView {
    private val tick: StateTick = StateTick(0L)
    private var idGenerator = 0
    private val entitiesStorage = DynamicEntitiesStorage(tick)
    val componentsStorage = ComponentsStorage(tick)
    private val removed = mutableListOf<EntityId>()
    private val operations = ArrayList<Operation>()

    private var delayRemoval = 0

    override fun getEntity(id: EntityId) : Entity? {
        if (!entitiesStorage.data.containsKey(id.id)) {
            return null
        }
        return getInternal(id)
    }

    fun atTick(tick: Long) : StateView {
        return object : StateView {
            override fun getEntity(id: EntityId): Entity? {
                if (!entitiesStorage.data.containsKey(id.id)) {
                    return null
                }
                return getInternal(id, tick)
            }
        }
    }

    private fun getInternal(id: EntityId, tick: Long): Entity {
        return object : Entity {
            override val id: EntityId = id
            override val ownedBy: Entity.OwnedBy = Entity.OwnedBy.System

            override fun <T : Component> hasComponent(clazz: KClass<T>): Boolean {
                val key = componentRegistry.getKey(clazz).key.toInt()
                return entitiesStorage.data.get(id.id).get(key) != -1
            }

            override fun <T : Component> getComponent(clazz: KClass<T>): T {
                val key = componentRegistry.getKey(clazz).key.toInt()
                val index = entitiesStorage.data.get(id.id).get(key)
                if (index == -1) {
                    throw IllegalStateException("Entity ${id.id} does not have component ${clazz.simpleName}")
                }
                return (componentsStorage.components.get(key) as ComponentStorage<T>).get(
                    index,
                    tick
                )
            }

            override fun <C : Component> addComponent(component: C) {
                TODO()
            }

            override fun <T : Component> removeComponent(clazz: KClass<T>) {
                TODO("Not yet implemented")
            }

            override fun components(): Collection<Component> {
                TODO("Not yet implemented")
            }
        }
    }

    private fun getInternal(id: EntityId) =
        object : Entity {
            override val id: EntityId = id
            override val ownedBy: Entity.OwnedBy = Entity.OwnedBy.System

            override fun <T : Component> hasComponent(clazz: KClass<T>): Boolean {
                val key = componentRegistry.getKey(clazz).key.toInt()
                return entitiesStorage.data.get(this.id.id).get(key) != -1
            }

            override fun <T : Component> getComponent(clazz: KClass<T>): T {
                val key = componentRegistry.getKey(clazz).key.toInt()
                val index = entitiesStorage.data.get(this.id.id).get(key)
                if (index == -1) {
                    throw IllegalStateException("Entity ${this.id.id} does not have component ${clazz.simpleName}")
                }
                return (componentsStorage.components.get(key) as ComponentStorage<T>).get(
                    index
                )
            }

            override fun <C : Component> addComponent(component: C) {
                val key = componentRegistry.getKey(component::class).key.toInt()
                val index =
                    (componentsStorage.components.get(key) as ComponentStorage<C>).add(
                        component,
                        this.id.id
                    )
                entitiesStorage.data.get(this.id.id).addComponent(key, index)
            }

            override fun <T : Component> removeComponent(clazz: KClass<T>) {
                val key = componentRegistry.getKey(clazz).key.toInt()
                val componentStorage = componentsStorage.components[key]
                if (componentStorage.delayRemoval > 0) {
                    operations.add(Operation.RemoveComponent(this.id.id, key))
                } else {
                    removeComponentInternal(this.id.id, key)
                }
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
        val index = entitiesStorage.data.get(entityId).get(key)
        entitiesStorage.data.get(entityId).removeComponent(key)
        val newEntity = componentStorage.remove(index)
        if (newEntity == -1) {
            return
        }
        entitiesStorage.data.get(newEntity).set(key, index)

    }

    private fun removeInternal(id: EntityId) {
        val components = entitiesStorage.data.get(id.id)
        // TODO invent a way to not allocate an iterator
        components.all().forEach { entry ->
            val key = entry.key
            val index = entry.value
            componentsStorage.components[key].removeInternal(index)
        }
        entitiesStorage.data.remove(id.id)
    }

    fun createEntity(): Entity {
        val id = idGenerator++
        entitiesStorage.create(id)
        return getInternal(EntityId(id))
    }

    fun queryAndExecute(clazz: KClass<out Component>, block: (EntityId, Component) -> Unit) {
        delayRemoval++
        val key = componentRegistry.getKey(clazz).key.toInt()
        val storage = componentsStorage.components[key]
        storage.delayRemoval++
        val size = storage.size
        for (i in 0 until size) {
            val entityId = storage.getEntityId(i)
            block(EntityId(entityId), storage.get(i))
        }
        storage.flushRemovals()
        flushRemovals()
    }

    private fun flushRemovals() {
        if (--delayRemoval > 0) return
        for (id in removed) {
            removeInternal(id)
        }
        removed.clear()
    }

    private fun ComponentStorage<*>.flushRemovals() {
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
        storage.delayRemoval
        for (i in 0 until storage.size) {
            val entityId = storage.getEntityId(i)
            // use only those Entities which have both components:
            val entityComponents = entitiesStorage.data.get(entityId)
            val c1index = if (minStorageKey == key1) i else entityComponents.get(key1)
            if (c1index == -1) {
                continue
            }
            val c2index = if (minStorageKey == key2) i else entityComponents.get(key2)
            if (c2index == -1) {
                continue
            }
            val c1 = storage1.get(c1index)
            val c2 = storage2.get(c2index)
            block(EntityId(entityId), c1, c2)
        }
        storage.flushRemovals()
        flushRemovals()
    }

    fun advance() {
        entitiesStorage.advance()
        componentsStorage.advance()
    }

    private sealed interface Operation {
        data class RemoveComponent(val entity: Int, val key: Int) : Operation
    }
}
