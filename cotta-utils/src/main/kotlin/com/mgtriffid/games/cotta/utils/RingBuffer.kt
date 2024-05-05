package com.mgtriffid.games.cotta.utils

class RingBuffer<T>(private val capacity: Int) {
    private val data = Array<Node<T>?>(capacity) { Node(-1L, null) }

    private var lastSet = -1L

    operator fun set(key: Long, value: T) {
        data[(key % capacity).toInt()] = Node(key, value)
        lastSet = key
    }

    operator fun get(key: Long): T? = when {
        key > lastSet -> null
        key < lastSet - capacity + 1 -> null
        else -> {
            val node = data[(key % capacity).toInt()]
            when {
                node == null -> null
                node.key == key -> node.value
                else -> null
            }
        }
    }

    private class Node<T>(
        val key: Long,
        val value: T?
    )
}
