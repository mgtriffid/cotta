package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.Component
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import kotlin.reflect.KClass

internal class ArraysEntity(
    private val componentRegistry: ComponentRegistry,
    /*override*/ val id: EntityId,
    /*override*/ val ownedBy: Entity.OwnedBy,
    private val entitiesStorage: DynamicEntitiesStorage,
    private val componentsStorage: ComponentsStorage
)/* : Entity {

    override fun <T : Component<T>> hasComponent(clazz: KClass<T>): Boolean {
        val key = componentRegistry.getKey(clazz).key.toInt()

        return entitiesStorage.data.get(id.toInt()).hasComponent(key)
    }

//    @Suppress("UNCHECKED_CAST")
    override fun <T : Component<T>> getComponent(clazz: KClass<T>): T {
        val key = componentRegistry.getKey(clazz).key.toInt()
        val componentIndex = entitiesStorage.data.get(id.toInt()).get(key)
        if (componentIndex == -1) {
            throw RuntimeException("Entity $id does not have component ${clazz.simpleName}")
        }

        return componentsStorage.components.get(key).get(componentIndex) as T
    }

    override fun <C: Component<C>> addComponent(component: C) {
        val key = componentRegistry.getKey(component::class).key.toInt()
        val index = componentsStorage.addComponent(key, id.toInt(), component)
        entitiesStorage.data.get(id.toInt()).addComponent(key, index)
    }

    override fun <T : Component<T>> removeComponent(clazz: KClass<T>) {
        TODO("Not yet implemented")
    }

    override fun components(): Collection<Component<*>> {
        TODO("Not yet implemented")
    }
}
*/
fun EntityId.toInt(): Int = id
