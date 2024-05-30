package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.Component

// not thread-safe
class ComponentStorage<C : Component>(
    private val data: Data<C>
) {
    var delayRemoval: Int = 0
    private var entities = IntArray(8)
    var size = 0
        private set

    private val operations = ArrayList<Operation<C>>()

    fun add(value: C, entity: Int): Int {
        ensureCapacity()
        data[size] = value
        entities[size] = entity
        return size++
    }

    fun advance() {
        if (data is HistoricalData) {
            data.advance()
        }
    }

    fun remove(index: Int): Int {
        size--
        data.remove(index, size)
        val newEntity = entities[size]
        entities[index] = newEntity
        return if (size == 0) -1 else newEntity
    }

    fun get(index: Int) = data[index]

    fun get(index: Int, tick: Long) = if (data is HistoricalData) {
        data.get(index, tick)
    } else {
        data[index]
    }

    fun getEntityId(index: Int) = entities[index]

    fun addInternal(value: C, entity: Int) {
        ensureCapacity()
        data[size] = value
        entities[size] = entity
        size++
    }

    fun removeInternal(index: Int): Int {
        size--
        data.remove(index, size)
        val newEntity = entities[size]
        entities[index] = newEntity
        return if (size == 0) -1 else newEntity
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
        data class Add<C : Component>(val value: C, val entity: Int) :
            Operation<C>

        data class Remove<C>(val index: Int) : Operation<C>
    }

    interface Data<C> {
        fun grow()

        operator fun set(index: Int, component: C)
        fun remove(index: Int, size: Int)
        operator fun get(index: Int): C
    }

    interface HistoricalData<C> : Data<C> {
        fun advance()
        fun get(index: Int, tick: Long): C
    }
}
