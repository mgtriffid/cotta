package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.MutableComponent
import kotlin.reflect.KClass

class EntityImpl(override val id: EntityId) : Entity {
    val components = ArrayList<Component<*>>()
    override fun <T : Component<T>> hasComponent(clazz: KClass<T>): Boolean {
        return components.any { clazz.isInstance(it) }
    }

    override fun <T : Component<T>> getComponent(clazz: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return components.first { clazz.isInstance(it) } as T
    }

    override fun addComponent(component: Component<*>) {
        components.add(component)
    }

    override fun <T : Component<T>> removeComponent(clazz: KClass<T>) {
        components.removeIf { clazz.isInstance(it) }
    }

    fun deepCopy(): Entity {
        val ret = EntityImpl(id)
        ret.components.addAll(components.map {
            when (it) {
                is MutableComponent<*> -> it.copy()
                is InputComponent<*> -> it.copy()
                else -> it
            }
        })
        return ret
    }

    override fun components(): Collection<Component<*>> {
        return components.toList()
    }
}
