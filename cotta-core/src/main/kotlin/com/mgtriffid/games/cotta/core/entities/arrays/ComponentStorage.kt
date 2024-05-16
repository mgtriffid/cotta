package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.Component

// not thread-safe
class ComponentStorage<C: Component>(
    private val data: Data<C>
){
    private var entities = IntArray(8)
    private var size = 0

    private val operations = ArrayList<Operation<C>>()

    fun add(value: C, entity: Int): Int {
        ensureCapacity()
        data[size] = value
        entities[size] = entity
        size++
        return size
    }

    fun remove(index: Int) {
        size--
        data.remove(index, size)
        entities[index] = entities[size]    }

    fun get(index: Int) = data[index]

    fun addInternal(value: C, entity: Int) {
        ensureCapacity()
        data[size] = value
        entities[size] = entity
        size++
    }

    fun removeInternal(index: Int) {
        size--
        data.remove(index, size)
        entities[index] = entities[size]
    }

    private fun ensureCapacity() {
        if (size == entities.size) {
            data.grow()
            val newEntities = IntArray(size * 2)
            System.arraycopy(entities, 0, newEntities, 0, size)
            entities = newEntities
        }
    }

    private sealed interface Operation<C> {
        data class Add<C: Component>(val value: C, val entity: Int) : Operation<C>
        data class Remove<C>(val index: Int) : Operation<C>
    }

    interface Data<C> {
        fun grow()

        operator fun set(index: Int, component: C)
        fun remove(index: Int, size: Int)
        operator fun get(index: Int): C
    }
}
