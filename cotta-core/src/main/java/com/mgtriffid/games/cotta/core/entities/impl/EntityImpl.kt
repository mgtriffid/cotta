package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.*
import kotlin.reflect.KClass

class EntityImpl(
    override val id: EntityId,
    override val ownedBy: Entity.OwnedBy,
) : Entity {
    private val components = ArrayList<Component<*>>()
    private val inputComponents = HashMap<KClass<out InputComponent<*>>, InputComponent<*>?>()

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
        return try {
            inputComponents[clazz] as T
        } catch (t: Throwable) {
            println("keke")
            throw t
        }
    }

    override fun <T : Component<T>> removeComponent(clazz: KClass<T>) {
        components.removeIf { clazz.isInstance(it) }
    }

    /**
     * Used only to advance the state from one tick to another, that's why copies components but leaves
     * inputComponents blank. Because they should be filled deliberately.
     */
    fun deepCopy(): Entity {
        val ret = EntityImpl(id, ownedBy)
        ret.components.addAll(components.map {
            when (it) {
                is MutableComponent<*> -> it.copy()
                else -> it
            }
        })
        inputComponents.keys.forEach { ret.inputComponents[it] = null}
        return ret
    }

    override fun components(): Collection<Component<*>> {
        return components.toList()
    }
}
