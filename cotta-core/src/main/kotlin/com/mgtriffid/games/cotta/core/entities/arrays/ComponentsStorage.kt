package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import kotlin.reflect.KClass

class ComponentsStorage(val tick: StateTick) {
    val components = ArrayList<ComponentStorage<*>>()

    fun <C: Component> addComponent(key: Int, id: Int, component: C): Int {
        return (components[key] as ComponentStorage<C>).add(component, id)
    }

    fun register(key: ShortComponentKey, kClass: KClass<out Component>) {
        val storage = kClass.qualifiedName?.let { Class.forName(it + "DataStorage").getConstructor().newInstance() }
        storage as ComponentStorage.Data<Component>
        components.add(key.key.toInt(), ComponentStorage(storage))
    }

    fun advance() {
        for (i in 0 until components.size) {
            components[i].advance()
        }
    }
}
