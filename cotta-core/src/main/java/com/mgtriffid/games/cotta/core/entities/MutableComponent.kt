package com.mgtriffid.games.cotta.core.entities

interface MutableComponent<T: MutableComponent<T>> : Component {
    fun copy(): T
}
