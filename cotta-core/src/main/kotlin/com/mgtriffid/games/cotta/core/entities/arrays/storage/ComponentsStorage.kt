package com.mgtriffid.games.cotta.core.entities.arrays.storage

import com.mgtriffid.games.cotta.core.codegen.Constants.DATA_STORAGE_SUFFIX
import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.arrays.StateTick
import com.mgtriffid.games.cotta.core.registry.ShortComponentKey
import kotlin.reflect.KClass

class ComponentsStorage(val tick: StateTick) {
    val components = ArrayList<ComponentStorage<*>>()
    var delayRemoval = 0

    fun <C: Component> addComponent(key: Int, id: Int, component: C): Int {
        return (components[key] as ComponentStorage<C>).add(component, id)
    }

    fun register(key: ShortComponentKey, kClass: KClass<out Component>) {
        val data = kClass.qualifiedName?.let { Class.forName(it + DATA_STORAGE_SUFFIX).getConstructor().newInstance() }
        data as ComponentStorage.Data<Component>
        components.add(key.key.toInt(), ComponentStorage(data))
    }

    fun advance() {
        for (i in 0 until components.size) {
            components[i].advance()
        }
    }
}
