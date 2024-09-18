package com.mgtriffid.games.cotta.core.entities.arrays.storage

import com.badlogic.gdx.utils.IntIntMap

internal class EntityComponents {
    private val regular = IntIntMap()
    private var index: Int = 0

    var tick: Long = 0
        set(value) {
            field = value
            index = (value % 8).toInt()
        }

    private val historical = Array(8) { IntIntMap() }

    fun addComponent(componentType: Int, index: Int, isHistorical: Boolean) {
        if (isHistorical) {
            historical[(tick % 8).toInt()].put(componentType, index)
        } else {
            regular.put(componentType, index)
        }
    }

    fun get(componentType: Int): Int {
        return regular.get(componentType, -1)
    }

    fun getHistorical(componentType: Int, tick: Long): Int {
        return historical[(tick % 8).toInt()].get(componentType, -1)
    }

    fun advance() {
        val historicalData = historical[index]
        tick++
        historical[index] = IntIntMap(historicalData)
    }

    fun removeComponent(componentType: Int) {
        regular.remove(componentType, -1)
    }

    fun hasComponent(componentType: Int): Boolean {
        return regular.containsKey(componentType)
    }

    fun all(): IntIntMap {
        return regular
    }

    fun set(key: Int, index: Int) {
        regular.put(key, index)
    }
}
