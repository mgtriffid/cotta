package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.Component

class ComponentsStorage(val tick: StateTick) {
    val components = ArrayList<ComponentStorage<*>>()

    fun <C: Component> addComponent(key: Int, id: Int, component: C): Int {
        return (components[key] as ComponentStorage<C>).add(component, id)
    }

    fun register() {
        // add a storage
    }
}
