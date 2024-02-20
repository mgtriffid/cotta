package com.mgtriffid.games.cotta.core.entities.impl

import com.badlogic.gdx.utils.IntMap
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.MutableComponent
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.exceptions.EcsRuntimeException
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import kotlin.reflect.KClass

class EntityImpl(
    private val componentRegistry: ComponentRegistry,
    override val id: EntityId,
    override val ownedBy: Entity.OwnedBy,
) : Entity {
    private val components = IntMap<Component<*>>()
    private val inputComponents = HashMap<KClass<out InputComponent<*>>, InputComponent<*>?>()

    override fun <T : Component<T>> hasComponent(clazz: KClass<T>): Boolean {
        return components.containsKey(componentRegistry.getKey(clazz).key.toInt())
    }

    override fun <T : Component<T>> getComponent(clazz: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return components.get(componentRegistry.getKey(clazz).key.toInt()) as? T
            ?: throw EcsRuntimeException("No such component")
    }

    override fun addComponent(component: Component<*>) {
        components.put(componentRegistry.getKey(component::class).key.toInt(), component)
    }

    override fun <T : InputComponent<T>> addInputComponent(clazz: KClass<T>) {
        inputComponents[clazz] = null
    }

    override fun hasInputComponents(): Boolean {
        return inputComponents.isNotEmpty()
    }

    override fun inputComponents(): Collection<KClass<out InputComponent<*>>> {
        return inputComponents.keys
    }

    override fun setInputComponent(clazz: KClass<out InputComponent<*>>, component: InputComponent<*>) {
        inputComponents[clazz] = component
    }

    override fun <T : InputComponent<T>> getInputComponent(clazz: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return inputComponents[clazz] as T
    }

    override fun <T : Component<T>> removeComponent(clazz: KClass<T>) {
        components.remove(componentRegistry.getKey(clazz).key.toInt())
    }

    /**
     * Used only to advance the state from one tick to another, that's why copies components but leaves
     * inputComponents blank. Because they should be filled deliberately.
     */
    fun deepCopy(): Entity {
        val ret = EntityImpl(componentRegistry, id, ownedBy)
        components.forEach { entry ->
            if (entry.value is MutableComponent<*>) {
                val key = entry.key
                val value = (entry.value as MutableComponent<*>).copy()
                ret.components.put(key, value)
            } else {
                ret.components.put(entry.key, entry.value)
            }
        }
        inputComponents.keys.forEach { ret.inputComponents[it] = null }
        return ret
    }

    override fun components(): Collection<Component<*>> {
        return components.values().toList()
    }
}
