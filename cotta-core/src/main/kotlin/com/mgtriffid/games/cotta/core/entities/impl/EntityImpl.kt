package com.mgtriffid.games.cotta.core.entities.impl

import com.badlogic.gdx.utils.IntMap
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.MutableComponent
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.exceptions.EcsRuntimeException
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import kotlin.reflect.KClass

class EntityImpl(
    private val componentRegistry: ComponentRegistry,
    override val id: EntityId,
    override val ownedBy: Entity.OwnedBy,
) : Entity {
    private var components = IntMap<Component<*>>()
    private var historicalComponents = IntMap<Component<*>>()

    override fun <T : Component<T>> hasComponent(clazz: KClass<T>): Boolean {
        val key = componentRegistry.getKey(clazz)
        return components(key).containsKey(key.key.toInt())
    }

    override fun <T : Component<T>> getComponent(clazz: KClass<T>): T {
        val key = componentRegistry.getKey(clazz)
        @Suppress("UNCHECKED_CAST")
        return components(key).get(key.key.toInt()) as? T
            ?: throw EcsRuntimeException("No such component")
    }

    override fun addComponent(component: Component<*>) {
        val key = componentRegistry.getKey(component::class)
        components(key).put(key.key.toInt(), component)
    }

    private fun components(key: ShortComponentKey) =
        if (componentRegistry.isHistorical(key)) {
            historicalComponents
        } else {
            components
        }

    override fun <T : Component<T>> removeComponent(clazz: KClass<T>) {
        val key = componentRegistry.getKey(clazz)
        components(key).remove(componentRegistry.getKey(clazz).key.toInt())
    }

    /**
     * Used only to advance the state from one tick to another, that's why copies components but leaves
     * inputComponents blank. Because they should be filled deliberately.
     */
    fun deepCopy(): Entity {
        val ret = EntityImpl(componentRegistry, id, ownedBy)
        ret.components = components
        historicalComponents.forEach { entry ->
            val value = (entry.value as MutableComponent<*>).copy()
            ret.historicalComponents.put(entry.key, value)
        }
        return ret
    }

    override fun components(): Collection<Component<*>> {
        return components.values().toList() + historicalComponents.values().toList()
    }
}
