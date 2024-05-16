package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import kotlin.reflect.KClass

interface Entity {
    val id: EntityId
    val ownedBy: OwnedBy
    fun <T: Component> hasComponent(clazz: KClass<T>) : Boolean
    fun <T: Component> getComponent(clazz: KClass<T>): T
    fun <C: Component> addComponent(component: C)
    fun <T: Component> removeComponent(clazz: KClass<T>)
    fun components(): Collection<Component>

    sealed class OwnedBy {
        object System : OwnedBy()
        // if ever becomes mutable be aware of that deepCopy method of EntityImpl
        data class Player(val playerId: PlayerId) : OwnedBy()
    }
}
