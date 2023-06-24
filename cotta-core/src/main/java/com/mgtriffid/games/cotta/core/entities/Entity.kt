package com.mgtriffid.games.cotta.core.entities

import kotlin.reflect.KClass

interface Entity {
    val id: EntityId
    val ownedBy: OwnedBy
    fun <T: Component<T>> hasComponent(clazz: KClass<T>) : Boolean
    fun <T: Component<T>> getComponent(clazz: KClass<T>): T
    fun addComponent(component: Component<*>)
    fun <T: InputComponent<T>> addInputComponent(clazz: KClass<T>)
    fun hasInputComponents(): Boolean
    fun hasInputComponent(clazz: KClass<out InputComponent<*>>): Boolean = inputComponents().contains(clazz)
    fun inputComponents(): Collection<KClass<out InputComponent<*>>>
    fun setInputComponent(clazz: KClass<out InputComponent<*>>, component: InputComponent<*>)
    fun <T: InputComponent<T>> getInputComponent(clazz: KClass<T>): T
    fun <T: Component<T>> removeComponent(clazz: KClass<T>)
    fun components(): Collection<Component<*>>

    sealed class OwnedBy {
        object System : OwnedBy()
        // if ever becomes mutable be aware of that deepCopy method of EntityImpl
        data class Player(val playerId: PlayerId) : OwnedBy()
    }
}
