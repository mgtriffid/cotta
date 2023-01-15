package com.mgtriffid.games.cotta.core.entities

import kotlin.reflect.KClass

interface Entity {
    val id: Int
    fun <T: Component<T>> hasComponent(clazz: KClass<T>) : Boolean
    fun <T: Component<T>> getComponent(clazz: KClass<T>): T
    fun <T: Component<T>> addComponent(component: T)
    fun <T: Component<T>> removeComponent(clazz: KClass<T>)
}
