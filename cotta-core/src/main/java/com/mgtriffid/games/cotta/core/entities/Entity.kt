package com.mgtriffid.games.cotta.core.entities

import kotlin.reflect.KClass

interface Entity {
    val id: Int
    fun <T: Component> hasComponent(clazz: KClass<T>) : Boolean
    fun <T: Component> getComponent(clazz: KClass<T>): T
    fun <T: Component> addComponent(component: T)
    fun <T: Component> removeComponent(clazz: KClass<T>)
}
